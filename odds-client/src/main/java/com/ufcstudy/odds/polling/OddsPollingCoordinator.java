package com.ufcstudy.odds.polling;

import com.ufcstudy.odds.model.ProviderQuota;
import com.ufcstudy.odds.model.SportsbookOddsBatch;
import com.ufcstudy.odds.model.SportsbookOddsRequest;
import com.ufcstudy.odds.provider.SportsbookOddsClient;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class OddsPollingCoordinator {

    private final SportsbookOddsClient client;
    private final SportsbookOddsRequest request;
    private final long lowQuotaThreshold;

    private final AtomicBoolean running =
            new AtomicBoolean(false);

    private final AtomicReference<ProviderQuota> latestQuota =
            new AtomicReference<>();

    public OddsPollingCoordinator(
            SportsbookOddsClient client,
            SportsbookOddsRequest request,
            long lowQuotaThreshold
    ) {
        this.client = Objects.requireNonNull(client);
        this.request = Objects.requireNonNull(request);

        if (lowQuotaThreshold < 0) {
            throw new IllegalArgumentException(
                    "Low quota threshold cannot be negative"
            );
        }

        this.lowQuotaThreshold = lowQuotaThreshold;
    }

    public OddsPollingResult poll() {
        ProviderQuota quota = latestQuota.get();

        if (quota != null
                && quota.isAtOrBelow(lowQuotaThreshold)) {
            return OddsPollingResult.skipped(
                    OddsPollingResult.Status.SKIPPED_LOW_QUOTA,
                    "Provider quota is at or below the safety threshold"
            );
        }

        if (!running.compareAndSet(false, true)) {
            return OddsPollingResult.skipped(
                    OddsPollingResult.Status.SKIPPED_ALREADY_RUNNING,
                    "A sportsbook request is already running"
            );
        }

        try {
            SportsbookOddsBatch batch =
                    client.fetchCurrentOdds(request);

            latestQuota.set(batch.quota());

            return OddsPollingResult.completed(batch);
        } finally {
            running.set(false);
        }
    }

    public ProviderQuota latestQuota() {
        return latestQuota.get();
    }
}