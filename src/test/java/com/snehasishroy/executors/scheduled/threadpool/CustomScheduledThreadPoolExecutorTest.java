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
    @Test
    public void testScheduleAtFixedRate() throws InterruptedException {
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
}
