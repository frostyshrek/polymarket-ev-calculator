package com.ufcstudy.odds.polling;

import java.time.Duration;

public final class ThreadSleeper implements Sleeper {

    @Override
    public void sleep(Duration duration)
            throws InterruptedException {

        Thread.sleep(duration);
    }
}