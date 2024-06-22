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
                    Runnable work = workQueue.take();
                    log.info("Task found by worker {}", Thread.currentThread().getName());
                    work.run();
                    log.info("Execution completed by worker {}", Thread.currentThread().getName());
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
    public boolean submit(Runnable runnable) {
        return workQueue.offer(runnable);
    }


}
