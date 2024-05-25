package org.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WaitNotifySemaphore implements CustomSemaphore {

    private final Object lock;
    volatile int tokens;

    public WaitNotifySemaphore(int count) {
        tokens = count;
        lock = new Object();
    }

    @Override
    public void acquire() throws InterruptedException {
        synchronized (lock) {
            while (tokens <= 0) {
                // while loop is critical to avoid any spurious wakeups
                // in my initial implementation, I missed adding while loop which caused the tests to fail because the acquire
                // method used to simply return without doing anything and the test used to release the lock causing more tokens than allowed
                log.info("No token available, waiting for someone to release");
                lock.wait();
            }
            tokens--;
            log.info("Acquired token, prev {}, cur {}", tokens + 1, tokens);
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
