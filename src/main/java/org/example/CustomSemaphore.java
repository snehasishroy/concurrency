package org.example;

public interface CustomSemaphore {
    void acquire() throws InterruptedException;
    void release();
}
