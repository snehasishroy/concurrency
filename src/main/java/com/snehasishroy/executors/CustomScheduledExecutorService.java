package com.snehasishroy.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public interface CustomScheduledExecutorService extends CustomExecutorService {
    Future<Void> scheduleAtFixedRate(Runnable runnable, int repeatAfter, TimeUnit unit) throws InterruptedException;

    <T> Future<T> scheduleAtFixedRate(Callable<T> runnable, int repeatAfter, TimeUnit unit) throws InterruptedException;
}
