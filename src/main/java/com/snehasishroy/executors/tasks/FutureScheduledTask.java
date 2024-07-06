package com.snehasishroy.executors.tasks;

import lombok.extern.slf4j.Slf4j;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class FutureScheduledTask<T> implements Future<T>, Runnable {
    private final Queue<Runnable> queue;
    private final Callable<T> callable;
    private final long repeatAfterNanos; // repeat after
    private final long executionAtNanos; // scheduled execution time
    private T result; // stored result

    private final Lock lock = new ReentrantLock();

    public FutureScheduledTask(Callable<T> callable, long repeatAfterNanos, Queue<Runnable> queue) {
        this.callable = callable;
        this.repeatAfterNanos = repeatAfterNanos;
        executionAtNanos = repeatAfterNanos + System.nanoTime();
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            T result = callable.call();
            lock.lock();
            this.result = result;
            lock.unlock();
            if (repeatAfterNanos > 0) {
                // add it back to the queue
                queue.add(new FutureScheduledTask<T>(callable, repeatAfterNanos, queue));
            }
        } catch (Exception e) {
            log.error("Error while calling the callable", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }
}
