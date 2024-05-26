package com.snehasishroy.semaphore;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WaitNotifySemaphore implements CustomSemaphore {

    private final Object lock;
    /**
     * No point in making this volatile as we already have a lock that causes happens before relationship.
     * Synchronized methods and blocks provide mutual exclusion guarantees and visibility guarantees i.e. that the changes made by one
     * thread to the shared data are visible to other threads to maintain data consistency.
     * Volatile is useful in scenarios where there is no mutual exclusion, but you still want the visibility aspect of the data change.
     * Volatile keyword introduces the happens before relationship - anything that has happened before writing to a volatile field, will be
     * visible to the another thread reading the same volatile field. So if there is only one volatile field and thread 1 makes changes to 3 other
     * non-volatile fields, post which a second thread reads the same volatile field - changes to other non-volatile fields will also be visible.
     *
     * Practically volatile should be used when setting a flag or a value - as a simple signalling mechanism - so that value to is visible to other threads.
     * If you are doing a read, modify, write, then mutual exclusion should be used to avoid lost updates.
     * <a href="https://www.baeldung.com/java-volatile">Reference</a>
     */
    private int tokens; //

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
                // method used to simply return without doing anything (in case of spurious wakeups) and the test used to
                // release the lock causing more tokens than allowed.
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
            lock.notify(); // No point in calling notifyAll() as only one
        }
    }
}
