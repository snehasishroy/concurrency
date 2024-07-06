package com.snehasishroy.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public interface CustomScheduledExecutorService extends CustomExecutorService {
    void scheduleAtFixedRate(Runnable runnable, int repeatAfter, TimeUnit unit) throws InterruptedException;

    <T> void scheduleAtFixedRate(Callable<T> runnable, int repeatAfter, TimeUnit unit) throws InterruptedException;
}
