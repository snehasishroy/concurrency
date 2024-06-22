package com.snehasishroy.executors;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class CustomThreadPoolExecutor implements CustomExecutorService {
    private final LinkedBlockingQueue<Runnable> workQueue;
    private final List<Thread> threads;

    public CustomThreadPoolExecutor(int limit) {
        this.workQueue = new LinkedBlockingQueue<>(limit);
        threads = new ArrayList<>(limit);
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

    public <T> Future<T> submit(Callable<T> callable) {
        FutureWork<T> future = new FutureWork<>(callable);
        workQueue.offer(future);
        return future;
    }

    private static class FutureWork<T> implements Future<T>, Runnable {
        private final Callable<T> callable;
        private T result;

        public FutureWork(Callable<T> callable) {
            this.callable = callable;
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
            return result;
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return result;
        }

        @Override
        public void run() {
            try {
                result = callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
