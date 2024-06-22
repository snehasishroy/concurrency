package com.snehasishroy.executors;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class CustomThreadPoolExecutor implements CustomExecutorService {
    private final LinkedBlockingQueue<Runnable> workQueue;

    public CustomThreadPoolExecutor(int limit) {
        this.workQueue = new LinkedBlockingQueue<>(limit);
        List<Thread> threads = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            threads.add(new Thread(() -> {
                try {
                    while (true) {
                        // keep polling the queue for the tasks and block if there are no tasks present
                        Runnable work = workQueue.take();
                        log.info("Task found by worker {}, queue size {}", Thread.currentThread().getName(), workQueue.size());
                        work.run();
                        log.info("Execution completed by worker {}, queue size {}", Thread.currentThread().getName(), workQueue.size());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, "worker-" + i));
        }
        // wait for all the threads to be created before starting them
        for (Thread thread : threads) {
            thread.start();
        }
    }

    /**
     * If the queue is full, then this method should return false
     */
    @Override
    public boolean execute(Runnable runnable) {
        return workQueue.offer(runnable);
    }

    @Override
    public <T> boolean execute(Callable<T> callable) {
        return workQueue.offer(new FutureWork<>(callable));
    }

    public <T> Future<T> submit(Callable<T> callable) throws InterruptedException {
        FutureWork<T> future = new FutureWork<>(callable);
        workQueue.put(future);
        return future;
    }

    private static class FutureWork<T> implements Future<T>, Runnable {
        private final Callable<T> callable;
        private T result;
        private final Object lock;

        public FutureWork(Callable<T> callable) {
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
}
