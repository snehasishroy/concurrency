package com.snehasishroy.executors;

import java.util.concurrent.Future;

public interface CustomExecutorService {
    /**
     * Returns true if the runnable is successfully submitted, else returns false
     */
    boolean execute(Runnable runnable);

    /**
     * Returns a future of the submitted task
     */
    Future<?> submit(Runnable runnable);
}
