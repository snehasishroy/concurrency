package com.snehasishroy.executors;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class CustomThreadPoolExecutorRunnableTest {

    @Test
    public void testSingleExecute() throws InterruptedException {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(1);
        CountDownLatch latch = new CountDownLatch(2);
        Stopwatch timer = Stopwatch.createStarted();
        for (int i = 0; i < 2; i++) {
            while (true) {
                // try submitting the task until successful
                if (executor.execute(getRunnable(i, latch))) {
                    log.info("Task submitted");
                    break;
                }
            }
        }
        latch.await();
        Duration time = timer.elapsed();
        log.info("Task executed");
        log.info("Time taken {} seconds", time.toSeconds());
        // each task takes around 1 sec to execute so 2 tasks should complete between 2 and 3 seconds
        assertTrue((time.toSeconds() >= 2) && (time.toSeconds() <= 3));
    }

    private static Runnable getRunnable(int taskID, CountDownLatch latch) {
        return () -> {
            log.info("Executing Task {}", taskID);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.info("Executed Task {}", taskID);
                latch.countDown();
            }
        };
    }

    @Test
    public void testMultipleExecute() throws InterruptedException {
        CustomThreadPoolExecutor executor = new CustomThreadPoolExecutor(2);
        // submit 5 tasks to a pool of 2 threads
        AtomicInteger counter = new AtomicInteger();
        List<Integer> res = new ArrayList<>();
        CountDownLatch allDone = new CountDownLatch(5);
        for (int i = 0; i < 5; i++) {
            while (true) {
                if (executor.execute(getRunnable(counter, res, i, allDone))) {
                    break;
                }
            }
        }
        allDone.await();
        int max = 0;
        for (Integer val : res) {
            max = Math.max(val, max);
        }
        Assertions.assertEquals(2, max);
    }

    private static Runnable getRunnable(AtomicInteger counter, List<Integer> res, int id, CountDownLatch allDone) {
        return () -> {
            int count = counter.incrementAndGet();
            res.add(count);
            log.info("Task {} running", id);
            counter.decrementAndGet();
            allDone.countDown();
            log.info("Finished task {}", id);
        };
    }
}
