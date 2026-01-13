package zzik2.soop4j.http;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import zzik2.soop4j.constant.SoopConstants;
import zzik2.soop4j.exception.SoopException;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

public class SoopHttpClient {

    private final HttpClient httpClient;
    private final Gson gson;
    private final String userAgent;

    public SoopHttpClient() {
        this(SoopConstants.DEFAULT_USER_AGENT);
    }

    public SoopHttpClient(String userAgent) {
        this.userAgent = userAgent;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public JsonObject get(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", userAgent)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            validateResponse(response, url);
            return parseJsonObject(response.body(), url);
        } catch (SoopException e) {
            throw e;
        } catch (Exception e) {
            throw new SoopException("GET 요청 실패: " + url, e);
        }
    }

    public JsonObject postForm(String url, Map<String, String> formData) {
        try {
            String body = formData.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                            + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", userAgent)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            validateResponse(response, url);
            return parseJsonObject(response.body(), url);
        } catch (SoopException e) {
            throw e;
        } catch (Exception e) {
            throw new SoopException("POST 요청 실패: " + url, e);
        }
    }

    private void validateResponse(HttpResponse<String> response, String url) {
        int statusCode = response.statusCode();
        if (statusCode >= 400) {
            String message = String.format("HTTP 요청 실패 (상태 코드: %d): %s", statusCode, url);
            throw new SoopException(message);
        }
    }

    private JsonObject parseJsonObject(String body, String url) {
        try {
            JsonObject result = gson.fromJson(body, JsonObject.class);
            if (result == null) {
                throw new SoopException("JSON 파싱 실패: " + url);
            }
            return result;
        } catch (SoopException e) {
            throw e;
        } catch (Exception e) {
            throw new SoopException("JSON 파싱 실패: " + url, e);
        }
    }

    public Gson getGson() {
        return gson;
    }
}
