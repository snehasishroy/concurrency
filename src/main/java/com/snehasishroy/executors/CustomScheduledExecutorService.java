package com.snehasishroy.executors;

import java.util.concurrent.TimeUnit;

public interface CustomScheduledExecutorService extends CustomExecutorService {
    void scheduleAtFixedRate(Runnable runnable, int delay, TimeUnit unit);
}
