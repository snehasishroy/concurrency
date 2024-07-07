package com.snehasishroy.executors;

import com.snehasishroy.executors.tasks.FutureScheduledTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class CustomScheduledThreadPoolExecutor implements CustomScheduledExecutorService {

    private final PriorityQueue<FutureScheduledTask<?>> queue;
    Lock lock = new ReentrantLock();
    Condition doProcess = lock.newCondition();

    public CustomScheduledThreadPoolExecutor(int concurrency) {
        queue = new PriorityQueue<>(Comparator.comparingLong(FutureScheduledTask::getExecutionAtNanos));

        List<Thread> threads = new ArrayList<>(concurrency);
        for (int i = 0; i < concurrency; i++) {
            threads.add(new Thread(() -> {
                while (true) { // need to run the workers indefinitely
                    lock.lock();
                    FutureScheduledTask<?> runnable = null;
                    try {
                        while (queue.isEmpty()) {
                            try {
                                log.info("{} found queue as empty, awaiting signal", Thread.currentThread().getName());
                                doProcess.await();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        FutureScheduledTask<?> top = queue.peek();
                        long delayNanos = top.getDelayNanos();
                        log.info("{} found the top task with delay {} ms", Thread.currentThread().getName(), TimeUnit.NANOSECONDS.toMillis(delayNanos));
                        if (delayNanos <= 0) {
                            // negative delay indicates that the task needs to be immediately executed
                            runnable = queue.poll();
                        } else {
                            // we need to wait for delayNanos before executing the task
                            // during this time, an even higher priority task can also come, which will be signalled by the schedule() function
                            log.info("{} going for a sleep for {} ms until awakened", Thread.currentThread().getName(), TimeUnit.NANOSECONDS.toMillis(delayNanos));
                            doProcess.await(delayNanos, TimeUnit.NANOSECONDS);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        lock.unlock();
                    }
                    if (runnable != null) {
                        log.info("{} being executed by {}, queue size {}", runnable.getID(), Thread.currentThread().getName(), queue.size());
                        runnable.run();
                    }
                }
            }, "Worker-" + i));
        }
        for (Thread thread : threads) {
            thread.start();
        }
    }

    @Override
    public Future<Void> scheduleAtFixedRate(Runnable runnable, int repeatAfter, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            FutureScheduledTask<Void> future = new FutureScheduledTask<>(runnable, unit.toNanos(repeatAfter), queue);
            queue.add(future);
            // signal all the waiting threads
            doProcess.signalAll();
            return future;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> Future<T> scheduleAtFixedRate(Callable<T> callable, int repeatAfter, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            FutureScheduledTask<T> future = new FutureScheduledTask<>(callable, unit.toNanos(repeatAfter), queue);
            queue.add(future);
            doProcess.signalAll();
            return future;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean execute(Runnable runnable) {
        return false;
    }

    @Override
    public <T> boolean execute(Callable<T> runnable) {
        return false;
    }

    @Override
    public <T> Future<T> submit(Callable<T> runnable) throws InterruptedException {
        return null;
    }

}
