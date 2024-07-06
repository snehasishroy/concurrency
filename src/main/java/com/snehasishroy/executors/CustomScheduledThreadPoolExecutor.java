package com.snehasishroy.executors;

import com.snehasishroy.executors.tasks.FutureScheduledTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.*;
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
                    log.info("Coming here");
                    lock.lock();
                    FutureScheduledTask<?> top;
                    try {
                        while (queue.isEmpty()) {
                            try {
                                log.info("{} found queue as empty, awaiting signal", Thread.currentThread().getName());
                                doProcess.await();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        top = queue.peek();
                        long delayNanos = top.getDelayNanos();
                        log.info("{} found the top task with delay {} ms", Thread.currentThread().getName(), TimeUnit.NANOSECONDS.toMillis(delayNanos));
                        if (delayNanos <= 0) {
                            queue.poll();
                        } else {
                            log.info("{} going for a sleep for {} ms until awakened", Thread.currentThread().getName(), TimeUnit.NANOSECONDS.toMillis(delayNanos));
                            doProcess.await(delayNanos, TimeUnit.NANOSECONDS);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } finally {
                        lock.unlock();
                    }
                    log.info("{} being executed by {}", top.getID(), Thread.currentThread().getName());
                    top.run();
                }
            }, "Worker-" + i));
        }
        for (Thread thread : threads) {
            thread.start();
        }
    }

    @Override
    public void scheduleAtFixedRate(Runnable runnable, int repeatAfter, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            queue.add(new FutureScheduledTask<>(runnable, unit.toNanos(repeatAfter), queue));
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> void scheduleAtFixedRate(Callable<T> callable, int repeatAfter, TimeUnit unit) throws InterruptedException {
        lock.lock();
        try {
            queue.add(new FutureScheduledTask<>(callable, unit.toNanos(repeatAfter), queue));
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
