package com.ufcstudy.odds.provider;

import com.ufcstudy.odds.exception.SportsbookClientException;
import com.ufcstudy.odds.exception.SportsbookRateLimitException;
import com.ufcstudy.odds.exception.SportsbookResponseException;
import com.ufcstudy.odds.model.SportsbookOddsBatch;
import com.ufcstudy.odds.model.SportsbookOddsRequest;
import com.ufcstudy.odds.polling.RetryPolicy;
import com.ufcstudy.odds.polling.Sleeper;

import java.time.Duration;
import java.util.Objects;

public final class RetryingSportsbookOddsClient
        implements SportsbookOddsClient {

    private final SportsbookOddsClient delegate;
    private final RetryPolicy retryPolicy;
    private final Sleeper sleeper;

    public RetryingSportsbookOddsClient(
            SportsbookOddsClient delegate,
            RetryPolicy retryPolicy,
            Sleeper sleeper
    ) {
        this.delegate = Objects.requireNonNull(delegate);
        this.retryPolicy = Objects.requireNonNull(retryPolicy);
        this.sleeper = Objects.requireNonNull(sleeper);
    }

    @Override
    public String providerCode() {
        return delegate.providerCode();
    }

    @Override
    public SportsbookOddsBatch fetchCurrentOdds(
            SportsbookOddsRequest request
    ) {
        int retriesCompleted = 0;

        while (true) {
            try {
                return delegate.fetchCurrentOdds(request);
            } catch (SportsbookRateLimitException exception) {
                if (retriesCompleted >= retryPolicy.maximumRetries()) {
                    throw exception;
                }

                retriesCompleted++;

                Duration delay = max(
                        exception.suggestedDelay(),
                        retryPolicy.delayForAttempt(retriesCompleted)
                );

                sleep(delay);
            } catch (SportsbookResponseException exception) {
                if (!isRetryableStatus(exception.statusCode())
                        || retriesCompleted
                        >= retryPolicy.maximumRetries()) {
                    throw exception;
                }

                retriesCompleted++;

                sleep(retryPolicy.delayForAttempt(retriesCompleted));
            }
        }
    }

    private void sleep(Duration duration) {
        try {
            sleeper.sleep(duration);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new SportsbookClientException(
                    "Retry wait was interrupted",
                    exception
            );
        }
    }

    private static boolean isRetryableStatus(int statusCode) {
        return statusCode == 500
                || statusCode == 502
                || statusCode == 503
                || statusCode == 504;
    }

    private static Duration max(Duration first, Duration second) {
        return first.compareTo(second) >= 0 ? first : second;
    }
}