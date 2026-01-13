package zzik2.soop4j.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class SoopExecutors {

    private static final Executor DEFAULT_EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "soop4j-async");
        t.setDaemon(true);
        return t;
    });

    private SoopExecutors() {
    }

    public static Executor defaultExecutor() {
        return DEFAULT_EXECUTOR;
    }
}
