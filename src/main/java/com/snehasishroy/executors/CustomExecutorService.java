package com.snehasishroy.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface CustomExecutorService {
    /**
     * Returns true if the runnable is successfully submitted, else returns false
     */
    boolean execute(Runnable runnable);

    /**
     * Returns true if the callable is successfully submitted, else returns false
     */
    <T> boolean execute(Callable<T> runnable);

    /**
     * Returns a future of the submitted task
     */
    <T> Future<T> submit(Callable<T> runnable);
}
