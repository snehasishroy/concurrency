package com.snehasishroy.executors;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CustomScheduledThreadPoolExecutorTest {
    @Test
    public void test() throws InterruptedException {
        CustomScheduledThreadPoolExecutor executor = new CustomScheduledThreadPoolExecutor(1);
        CountDownLatch isDone = new CountDownLatch(10);
        executor.scheduleAtFixedRate(() -> {
            log.info("Hello");
            isDone.countDown();
        }, 1, TimeUnit.SECONDS);
        isDone.await();

    }
}
