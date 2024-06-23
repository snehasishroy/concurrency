package com.snehasishroy.executors;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class CustomThreadPoolCallableTest {
    @Test
    public void testExecuteCallable() throws InterruptedException {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(1);
        CountDownLatch isDone = new CountDownLatch(1);
        boolean isSubmitted = executor.execute(() -> {
            isDone.countDown();
            return 5;
        });
        isDone.await();
        Assertions.assertTrue(isSubmitted);
        Assertions.assertEquals(0, isDone.getCount());
    }
}
