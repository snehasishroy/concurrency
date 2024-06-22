package com.snehasishroy.executors;

public interface CustomExecutorService {
    /**
     * Returns true if the runnable is successfully submitted, else returns false
     */
    boolean submit(Runnable runnable);
}
