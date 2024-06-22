package com.snehasishroy.executors;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CustomThreadPoolExecutorTest {

    @Test
    public void testSubmit() throws InterruptedException {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(1);
        CountDownLatch latch = new CountDownLatch(1);
        boolean firstTask = executor.submit(() -> {
            log.info("Running from executor");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        });
        boolean secondTask = executor.submit(() -> {
            log.info("This task should not be able to submit as the limit is 1");
        });
        latch.await();
        log.info("Task executed");
        assertTrue(firstTask);
        assertFalse(secondTask);
    }
}
