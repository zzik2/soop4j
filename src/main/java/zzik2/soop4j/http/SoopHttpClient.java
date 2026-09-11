package zzik2.soop4j.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import zzik2.soop4j.constant.SoopConstants;
import zzik2.soop4j.exception.SoopException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class SoopHttpClient {
    private static final ScheduledExecutorService TIMEOUTS = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "soop4j-http-timeouts");
        thread.setDaemon(true);
        return thread;
    });
    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final String userAgent;
    private final Duration requestTimeout;

    public SoopHttpClient() {
        this(SoopConstants.DEFAULT_USER_AGENT);
    }

    public SoopHttpClient(String userAgent) {
        this(userAgent, Duration.ofSeconds(15));
    }

    public SoopHttpClient(String userAgent, Duration requestTimeout) {
        this.userAgent = Objects.requireNonNull(userAgent, "userAgent");
        this.requestTimeout = Objects.requireNonNull(requestTimeout, "requestTimeout");
        if (requestTimeout.isZero() || requestTimeout.isNegative() || requestTimeout.toMillis() < 1) {
            throw new IllegalArgumentException("requestTimeout은 1ms 이상이어야 합니다");
        }
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public JsonObject get(String url) {
        return await(getAsync(url));
    }

    public JsonObject postForm(String url, Map<String, String> formData) {
        return await(postFormAsync(url, formData));
    }

    public CompletableFuture<JsonObject> getAsync(String url) {
        try {
            return send(request(url).GET().build());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(failure(url, e));
        }
    }

    public CompletableFuture<JsonObject> postFormAsync(String url, Map<String, String> formData) {
        try {
            String body = formData.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            return send(request(url).header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(body)).build());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(failure(url, e));
        }
    }

    private HttpRequest.Builder request(String url) {
        return HttpRequest.newBuilder(URI.create(url)).header("User-Agent", userAgent).timeout(requestTimeout);
    }

    private CompletableFuture<JsonObject> send(HttpRequest request) {
        CompletableFuture<JsonObject> result = new CompletableFuture<>();
        AtomicReference<Flow.Subscription> subscription = new AtomicReference<>();
        HttpResponse.BodyHandler<String> handler = info -> {
            HttpResponse.BodySubscriber<String> delegate = HttpResponse.BodyHandlers.ofString().apply(info);
            return new HttpResponse.BodySubscriber<String>() {

                public CompletionStage<String> getBody() {
                    return delegate.getBody();
                }

                public void onSubscribe(Flow.Subscription value) {
                    subscription.set(value);
                    delegate.onSubscribe(value);
                    if (result.isDone()) value.cancel();
                }

                public void onNext(List<ByteBuffer> items) {
                    delegate.onNext(items);
                }

                public void onError(Throwable error) {
                    delegate.onError(error);
                }

                public void onComplete() {
                    delegate.onComplete();
                }
            };
        };
        CompletableFuture<HttpResponse<String>> exchange = httpClient.sendAsync(request, handler);
        ScheduledFuture<?> deadline = TIMEOUTS.schedule(() -> result.completeExceptionally(
                failure(request.uri().toString(), new HttpTimeoutException("HTTP 응답 제한 시간 초과"))),
                requestTimeout.toMillis(), TimeUnit.MILLISECONDS);

        result.whenComplete((value, error) -> {
            deadline.cancel(false);
            if (error != null) {
                Flow.Subscription body = subscription.get();
                if (body != null) body.cancel();
                exchange.cancel(true);
            }
        });

        exchange.whenComplete((response, error) -> {
            if (result.isDone()) return;
            try {
                if (error != null) throw failure(request.uri().toString(), error);
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new SoopException("HTTP 요청 실패 (상태 코드: " + response.statusCode() + "): " + request.uri());
                }
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                if (json == null) throw new SoopException("비어 있는 JSON 응답: " + request.uri());
                result.complete(json);
            } catch (Exception e) {
                result.completeExceptionally(failure(request.uri().toString(), e));
            }
        });
        return result;
    }

    private static SoopException failure(String url, Throwable error) {
        while (error instanceof CompletionException || error instanceof ExecutionException) error = error.getCause();
        return error instanceof SoopException ? (SoopException) error : new SoopException("HTTP 요청 실패: " + url, error);
    }

    private static <T> T await(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new SoopException("HTTP 요청 중단", e);
        } catch (ExecutionException e) {
            throw failure("", e);
        }
    }

    public Gson getGson() {
        return gson;
    }
}
