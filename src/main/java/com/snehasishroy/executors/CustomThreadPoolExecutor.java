package com.snehasishroy.executors;

import com.snehasishroy.executors.tasks.FutureTask;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class CustomThreadPoolExecutor implements CustomExecutorService {
    private final LinkedBlockingQueue<Runnable> workQueue;

    public CustomThreadPoolExecutor(int limit) {
        this.workQueue = new LinkedBlockingQueue<>(limit);
        List<Thread> threads = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            threads.add(new Thread(() -> {
                try {
                    while (true) { // run the workers indefinitely
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
        return workQueue.offer(new com.snehasishroy.executors.tasks.FutureTask<>(callable));
    }

    public <T> Future<T> submit(Callable<T> callable) throws InterruptedException {
        com.snehasishroy.executors.tasks.FutureTask<T> future = new FutureTask<>(callable);
        workQueue.put(future);
        return future;
    }

}
