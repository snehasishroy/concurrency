package com.snehasishroy.executors;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
public class CustomScheduledThreadPoolExecutor implements CustomScheduledExecutorService {

    private final PriorityBlockingQueue<Runnable> queue;
    Semaphore semaphore;

    public CustomScheduledThreadPoolExecutor(int limit) {
        queue = new PriorityBlockingQueue<>(limit);
        semaphore = new Semaphore(limit);
        List<Thread> threads = new ArrayList<>(limit);
        for (int i = 0; i < limit; i++) {
            threads.add(new Thread(() -> {
                while (true) { // need to run the workers indefinitely
                    try {
                        Runnable work = queue.take();
                        work.run();
                    } catch (InterruptedException e) {
                        log.error("InterruptedException", e);
                        throw new RuntimeException(e);
                    } finally {
                        semaphore.release();
                    }
                }
            }));
        }
        for (Thread thread : threads) {
            thread.start();
        }
    }

    @Override
    public void scheduleAtFixedRate(Runnable runnable, int delay, TimeUnit unit) throws InterruptedException {


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
