package com.snehasishroy.executors;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * P.S: The type parameter <T> is not associated with the class, as it will restrict submission of tasks of only that
 * type to the execute and the submit method.
 * Now the type of execute and submit are independent.
 */
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
    <T> Future<T> submit(Callable<T> runnable) throws InterruptedException;
}
