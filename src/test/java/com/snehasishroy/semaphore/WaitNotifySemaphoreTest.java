package com.snehasishroy.semaphore;

import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class WaitNotifySemaphoreTest {

    @Test
    public void testSemaphoreTwoThreads() throws InterruptedException {
        CustomSemaphore semaphore = new WaitNotifySemaphore(1);
        CountDownLatch latch1 = new CountDownLatch(1);
        CountDownLatch latch2 = new CountDownLatch(1);
        log.info("Trying to acquire");
        AtomicBoolean isAcquired1 = new AtomicBoolean();
        AtomicBoolean isAcquired2 = new AtomicBoolean();
        Thread t1 = new Thread(() -> {
            try {
                latch1.await();
                log.info("Trying to acquire semaphore from thread1");
                semaphore.acquire();
                log.info("Semaphore acquired from thread1");
                latch2.countDown();
                isAcquired1.set(true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                latch2.await();
                log.info("Trying to acquire semaphore from thread2");
                semaphore.acquire();
                log.info("Semaphore acquired from thread2");
                isAcquired2.set(true);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        t1.start();
        t2.start();
        latch1.countDown(); // signal the first thread to start
        t1.join(); // wait for t1 to finish
        assertTrue(isAcquired1.get());
        Assertions.assertFalse(isAcquired2.get());
        Awaitility.waitAtMost(2, TimeUnit.SECONDS);
        Assertions.assertFalse(isAcquired2.get()); // wait for 2 seconds and assert whether t2 has still not acquired the semaphore
        semaphore.release(); // release the semaphore
        t2.join(); // wait for t2 to finish
        assertTrue(isAcquired2.get());
    }

    @Test
    public void testSemaphoreMultipleThreads() throws InterruptedException {
        WaitNotifySemaphore semaphore = new WaitNotifySemaphore(1);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger concurrentAcquistion = new AtomicInteger();
        AtomicInteger totalOps = new AtomicInteger();
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            new Thread(() -> {
                try {
                    latch.await(); // wait for all threads to be initialized
                    semaphore.acquire();
                    totalOps.incrementAndGet();
                    int val = concurrentAcquistion.incrementAndGet();
                    log.info("Val is {}", val);
                    res.add(val);
                    concurrentAcquistion.decrementAndGet();
                    semaphore.release();
                    totalOps.incrementAndGet();
                } catch (InterruptedException e) {
                    log.error("Interrupted Exception", e);
                    throw new RuntimeException(e);
                }
            }).start();
        }
        latch.countDown();
        Thread.sleep(1000);
        res.forEach(val -> Assertions.assertEquals(1, val));
    }
}
