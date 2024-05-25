package com.snehasishroy.semaphore;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class CASSemaphore implements CustomSemaphore {
    final AtomicInteger tokens;

    public CASSemaphore(int count) {
        tokens = new AtomicInteger(count);
    }

    @Override
    public void acquire() throws InterruptedException {
        while (true) {
            int available = tokens.get();
            int remaining = available - 1;
            if (remaining >= 0 && tokens.compareAndSet(available, remaining)) {
                return;
            }
        }
    }

    @Override
    public void release() {
        tokens.incrementAndGet();
    }
}
