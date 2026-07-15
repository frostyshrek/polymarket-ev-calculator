package com.ufcstudy.odds.exception;

import java.time.Duration;

public final class SportsbookRateLimitException
        extends SportsbookClientException {

    private final Duration suggestedDelay;

    public SportsbookRateLimitException(
            String message,
            Duration suggestedDelay
    ) {
        super(message);
        this.suggestedDelay = suggestedDelay;
    }

    public Duration suggestedDelay() {
        return suggestedDelay;
    }
}