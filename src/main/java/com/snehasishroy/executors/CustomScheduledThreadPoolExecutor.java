package com.snehasishroy.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class CustomScheduledThreadPoolExecutor extends CustomThreadPoolExecutor implements CustomScheduledExecutorService {

    public CustomScheduledThreadPoolExecutor(int limit) {
        super(limit);
    }

    @Override
    public void scheduleAtFixedRate(Runnable runnable, int delay, TimeUnit unit) {

    }
}
