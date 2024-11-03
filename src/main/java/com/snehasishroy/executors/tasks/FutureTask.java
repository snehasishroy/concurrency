package com.snehasishroy.executors.tasks;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class FutureTask<T> implements Future<T>, Runnable, Task {
    private final Callable<T> callable;
    private volatile T result;
    private final Lock lock = new ReentrantLock();
    private final Condition isDone = lock.newCondition();
    private final String taskID = UUID.randomUUID().toString();

    public FutureTask(Callable<T> callable) {
        this.callable = callable;
        log.info("Submitting task with ID {}", taskID);
    }

    public FutureTask(Runnable runnable) {
        this.callable = () -> {
            runnable.run();
            return null;
        };
        log.info("Submitting task with ID {}", taskID);
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
        // double-checked locking idiom, this is required to avoid taking a lock everytime to get the state of the result.
        // Result must be volatile.
        // https://en.wikipedia.org/wiki/Double-checked_locking
        if (result == null) {
            lock.lockInterruptibly();
            // try/finally block is outside lock() to ensure we don't call unlock() even without having the lock()
            // https://stackoverflow.com/questions/31058681/java-locking-structure-best-pattern
            try {
                while (result == null) {
                    // wait until the result is computed
                    isDone.await();
                }
            } finally {
                lock.unlock();
            }
        }
        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        Stopwatch timer = Stopwatch.createStarted();
        long timeoutMillis = unit.toMillis(timeout);
        long timeRemainingMillis = timeoutMillis;
        if (result == null) {
            // if we have used synchronized, then timeout won't be possible
            if (!lock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException(String.format("Unable to acquire lock for the result in %d ms for TaskID %s", timeoutMillis, taskID));
            }
            try {
                while (result == null) {
                    long elapsedMillis = timer.elapsed(TimeUnit.MILLISECONDS);
                    timeRemainingMillis -= elapsedMillis;
                    if (timeRemainingMillis <= 0) {
                        throw new TimeoutException(String.format("Timed out from waiting for TaskID %s", taskID));
                    }
                    log.info("Waiting for the result to be computed with a timeout of {} ms for TaskID {}", timeRemainingMillis, taskID);
                    isDone.await(timeRemainingMillis, TimeUnit.MILLISECONDS);
                }
            } finally {
                lock.unlock();
            }
        }
        return result;
    }

    @Override
    public void run() {
        T temp;
        try {
            temp = callable.call();
            log.info("Result computed for TaskID {}", taskID);
        } catch (Exception ex) {
            log.error("Exception while executing the callable for TaskID {}", taskID, ex);
            temp = null;
        }
        lock.lock();
        try {
            result = temp;
            isDone.signal(); // this signals the thread that might be waiting on the get() method
            log.info("Result updated for TaskID {}", taskID);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String getID() {
        return taskID;
    }
}
