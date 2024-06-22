package com.snehasishroy.executors;

import java.util.concurrent.TimeUnit;

public interface CustomScheduledCustomExecutorService extends CustomExecutorService {
    void scheduleAtFixedRate(Runnable runnable, int delay, TimeUnit unit);
}
