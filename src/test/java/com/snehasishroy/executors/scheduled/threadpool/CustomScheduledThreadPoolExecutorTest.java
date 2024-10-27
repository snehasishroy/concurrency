package com.snehasishroy.executors.scheduled.threadpool;

import com.google.common.base.Stopwatch;
import com.snehasishroy.executors.CustomScheduledThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CustomScheduledThreadPoolExecutorTest {

    /**
     * Run a single task every 1 second for 5 times
     */
    @Test
    public void testSingleScheduleAtFixedRateWithSingleConcurrency() throws InterruptedException {
        CustomScheduledThreadPoolExecutor executor = new CustomScheduledThreadPoolExecutor(1);
        CountDownLatch isDone = new CountDownLatch(5);
        AtomicInteger count = new AtomicInteger();
        Stopwatch timer = Stopwatch.createStarted();
        executor.scheduleAtFixedRate(() -> {
            log.info("Hello, execution count {}", count.incrementAndGet());
            isDone.countDown();
        }, 1, TimeUnit.SECONDS);
        isDone.await();
        long seconds = timer.elapsed(TimeUnit.SECONDS);

        assertEquals(5, count.get());
        assertTrue(seconds >= 5 && seconds <= 6);
    }

    /**
     * Run two tasks every 1 second for 5 times with task execution of 1 second -
     * total time to execute both the tasks should be 10 seconds.
     */
    @Test
    public void testMultipleScheduleAtFixedRateWithSingleConcurrencyAndLongExecution() throws InterruptedException {
        CustomScheduledThreadPoolExecutor executor = new CustomScheduledThreadPoolExecutor(1);
        CountDownLatch isFirstDone = new CountDownLatch(5);
        CountDownLatch isSecondDone = new CountDownLatch(5);
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        Stopwatch timer = Stopwatch.createStarted();
        executor.scheduleAtFixedRate(() -> {
            log.info("Hello, execution first {}", first.incrementAndGet());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            isFirstDone.countDown();
        }, 1, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(() -> {
            log.info("World, execution first {}", second.incrementAndGet());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            isSecondDone.countDown();
        }, 1, TimeUnit.SECONDS);
        isFirstDone.await();
        isSecondDone.await();

        long seconds = timer.elapsed(TimeUnit.SECONDS);
        assertEquals(5, first.get());
        assertEquals(5, second.get());
        assertTrue(seconds >= 10 && seconds <= 11);
    }

    @Test
    public void testMultipleScheduleAtFixedRateWithSingleConcurrency() throws InterruptedException {
        CustomScheduledThreadPoolExecutor executor = new CustomScheduledThreadPoolExecutor(1);
        CountDownLatch isFirstDone = new CountDownLatch(5);
        CountDownLatch isSecondDone = new CountDownLatch(5);
        AtomicInteger first = new AtomicInteger();
        AtomicInteger second = new AtomicInteger();
        Stopwatch timer = Stopwatch.createStarted();
        executor.scheduleAtFixedRate(() -> {
            log.info("Hello, execution first {}", first.incrementAndGet());
            isFirstDone.countDown();
        }, 1, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(() -> {
            log.info("World, execution first {}", second.incrementAndGet());
            isSecondDone.countDown();
        }, 1, TimeUnit.SECONDS);
        isFirstDone.await();
        isSecondDone.await();

        long seconds = timer.elapsed(TimeUnit.SECONDS);
        assertEquals(5, first.get());
        assertEquals(5, second.get());
        assertTrue(seconds >= 5 && seconds <= 6);
    }
}
