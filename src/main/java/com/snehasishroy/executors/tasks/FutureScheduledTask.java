package com.snehasishroy.executors.tasks;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.PriorityQueue;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class FutureScheduledTask<T> implements Future<T>, Runnable, Task {
    private final PriorityQueue<FutureScheduledTask<?>> queue;
    private final Callable<T> callable;
    private final long repeatAfterNanos; // repeat after
    @Getter private final long executionAtNanos; // scheduled execution time
    private T result; // stored result
    private final String taskID;

    private final Lock lock = new ReentrantLock();

    public FutureScheduledTask(Callable<T> callable, long repeatAfterNanos, PriorityQueue<FutureScheduledTask<?>> queue) {
        this.callable = callable;
        this.repeatAfterNanos = repeatAfterNanos;
        executionAtNanos = repeatAfterNanos + System.nanoTime();
        this.queue = queue;
        taskID = UUID.randomUUID().toString();
        log.info("Submitting task with ID {} with expected delay {}, queue size {}", taskID, TimeUnit.NANOSECONDS.toMillis(executionAtNanos - System.nanoTime()), queue.size());
    }

    public FutureScheduledTask(Runnable runnable, long repeatAfterNanos, PriorityQueue<FutureScheduledTask<?>> queue) {
        this.callable = () -> {
            runnable.run();
            return null;
        };
        this.repeatAfterNanos = repeatAfterNanos;
        executionAtNanos = repeatAfterNanos + System.nanoTime();
        this.queue = queue;
        taskID = UUID.randomUUID().toString();
        log.info("Submitting task with ID {} with expected delay {}, queue size {}", taskID, TimeUnit.NANOSECONDS.toMillis(executionAtNanos - System.nanoTime()), queue.size());
    }

    @Override
    public void run() {
        try {
            log.info("Lag in execution for TaskID {} is {} ms", taskID, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - executionAtNanos));
            T result = callable.call();
            lock.lock();
            this.result = result;
            lock.unlock();
            if (repeatAfterNanos > 0) {
                // add it back to the queue
                log.info("Adding the TaskID {} back to the queue", taskID);
                queue.add(new FutureScheduledTask<>(callable, repeatAfterNanos, queue));
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

    @Override
    public String getID() {
        return taskID;
    }

    public long getDelayNanos() {
        return executionAtNanos - System.nanoTime();
    }
}
