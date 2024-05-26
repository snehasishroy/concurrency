package com.snehasishroy.semaphore;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public final class CASSemaphore implements CustomSemaphore {
    private final AtomicInteger tokens;

    public CASSemaphore(int count) {
        tokens = new AtomicInteger(count);
    }

    @Override
    public void acquire() throws InterruptedException {
        while (true) {
            int available = tokens.get();
            int remaining = available - 1;
            if (remaining >= 0) {
                log.info("Tokens are available, trying to perform a CAS");
                if (tokens.compareAndSet(available, remaining)) {
                    log.info("Tokens acquired using CAS");
                    return;
                }
            } else {
                log.info("No tokens available, Retrying");
            }
        }
    }

    @Override
    public void release() {
        tokens.incrementAndGet();
    }
}
