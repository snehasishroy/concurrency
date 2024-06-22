package com.snehasishroy.executors;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
public class CustomThreadPoolExecutorFutureTest {

    @Test
    public void testSingleFuture() throws ExecutionException, InterruptedException {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(1);
        Stopwatch timer = Stopwatch.createStarted();
        Future<Integer> future = executor.submit(() -> {
            Thread.sleep(1000);
            return 5;
        });
        int result = future.get();
        long seconds = timer.elapsed().toSeconds();
        assertEquals(5, result);
        assertEquals(1, seconds);
    }

    @Test
    public void testFutureTimeout() throws ExecutionException, InterruptedException, TimeoutException {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(1);
        Stopwatch timer = Stopwatch.createStarted();
        Future<Integer> future = executor.submit(() -> {
            Thread.sleep(2000);
            return 5;
        });
        Assertions.assertThrows(TimeoutException.class, () -> future.get(1, TimeUnit.SECONDS));
        long seconds = timer.elapsed().toSeconds();
        log.info("Took {} seconds", seconds);
    }

    @Test
    public void testListOfFutures() throws ExecutionException, InterruptedException {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(1);
        Stopwatch timer = Stopwatch.createStarted();
        List<Future<Integer>> futures = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int id = i;
            futures.add(executor.submit(() -> {
                Thread.sleep(1000);
                return id * 2;
            }));
        }
        for (int i = 0; i < 5; i++) {
            int res = futures.get(i).get();
            assertEquals(i * 2, res);
        }
        long seconds = timer.elapsed().toSeconds();
        log.info("Time took {} seconds", seconds);
        assertEquals(5, seconds);
    }
}
