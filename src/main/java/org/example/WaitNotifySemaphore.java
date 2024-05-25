package org.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaitNotifySemaphore implements CustomSemaphore {

    private final Object lock;
    int tokens;

    public WaitNotifySemaphore(int count) {
        tokens = count;
        lock = new Object();
    }

    @Override
    public void acquire() throws InterruptedException {
        synchronized (lock) {
            int cur = tokens;
            if (cur > 0) {
                tokens--;
                log.info("Acquired token");
            } else {
                log.info("No token available, waiting for someone to release");
                lock.wait();
            }
        }
    }

    @Override
    public void release() {
        synchronized (lock) {
            tokens++;
            log.info("Released token. Notifying the threads blocked");
            lock.notifyAll();
        }
    }
}
