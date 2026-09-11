package zzik2.soop4j.internal;

import zzik2.soop4j.exception.SoopException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public final class Futures {
    private Futures() {
    }

    public static Throwable unwrap(Throwable error) {
        while ((error instanceof CompletionException || error instanceof ExecutionException) && error.getCause() != null) {
            error = error.getCause();
        }
        return error;
    }

    public static <T> T await(CompletableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            throw new SoopException("작업 중단", e);
        } catch (ExecutionException e) {
            Throwable cause = unwrap(e);
            if (cause instanceof RuntimeException) throw (RuntimeException) cause;
            throw new SoopException("비동기 작업 실패", cause);
        }
    }

    public static <T, R> CompletableFuture<R> map(CompletableFuture<T> source, Function<T, R> mapper) {
        return compose(source, value -> CompletableFuture.completedFuture(mapper.apply(value)));
    }

    public static <T, R> CompletableFuture<R> compose(CompletableFuture<T> source, Function<T, CompletableFuture<R>> mapper) {
        CompletableFuture<R> result = new CompletableFuture<>();
        AtomicReference<CompletableFuture<R>> next = new AtomicReference<>();
        result.whenComplete((value, error) -> {
            if (result.isCancelled()) {
                source.cancel(true);
                CompletableFuture<R> child = next.get();
                if (child != null) child.cancel(true);
            }
        });

        source.whenComplete((value, error) -> {
            if (result.isDone()) return;
            if (error != null) {
                result.completeExceptionally(unwrap(error));
                return;
            }
            try {
                CompletableFuture<R> child = mapper.apply(value);
                next.set(child);
                if (result.isCancelled()) child.cancel(true);
                child.whenComplete((mapped, failure) -> {
                    if (failure == null) result.complete(mapped);
                    else result.completeExceptionally(unwrap(failure));
                });
            } catch (Exception e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }
}
