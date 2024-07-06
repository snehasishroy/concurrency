package com.snehasishroy.executors.tasks;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class FutureTask<T> implements Future<T>, Runnable {
    private final Callable<T> callable;
    private T result;
    private final Object lock;

    public FutureTask(Callable<T> callable) {
        this.callable = callable;
        lock = new Object();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        synchronized (lock) {
            while (result == null) {
                lock.wait();
            }
        }
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Stopwatch timer = Stopwatch.createStarted();
        long timeoutMillis = unit.toMillis(timeout);
        long timeRemainingMillis = timeoutMillis;
        synchronized (lock) {
            while (true) {
                lock.wait(timeRemainingMillis);
                log.info("Waking up from the lock");
                if (result != null) {
                    log.info("Result is present, returning it");
                    return result;
                }
                long elapsedMillis = timer.elapsed(TimeUnit.MILLISECONDS);
                log.info("Time elapsed {} ms", elapsedMillis);
                if (elapsedMillis >= timeoutMillis) {
                    throw new TimeoutException();
                }
                timeRemainingMillis = timeoutMillis - elapsedMillis;
                log.info("Time remaining {} ms", timeRemainingMillis);
            }
        }
    }

    @Override
    public void run() {
        try {
            T result = callable.call();
            synchronized (lock) {
                this.result = result;
                lock.notify();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
