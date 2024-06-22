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
        for (Thread thread : threads) {
            thread.start();
        }
    }

//    public static void main(String[] args) {
//        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
//        Executors.newFixedThreadPool(10).
//        Executors.newFixedThreadPool()
//        scheduledExecutorService.schedule();
//        scheduledExecutorService.scheduleAtFixedRate()
//
//    }

    @Override
    public boolean execute(Runnable runnable) {
        return workQueue.offer(runnable);
    }

    public Future<?> submit(Runnable runnable) {
        return null;
    }

    private class Work<V> implements Future<V> {

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
        public V get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }
}
