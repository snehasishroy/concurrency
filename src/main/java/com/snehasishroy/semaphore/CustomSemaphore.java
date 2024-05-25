package com.snehasishroy.semaphore;

public interface CustomSemaphore {
    void acquire() throws InterruptedException;
    void release();
}
