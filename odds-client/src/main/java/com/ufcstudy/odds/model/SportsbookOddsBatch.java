package com.ufcstudy.odds.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record SportsbookOddsBatch(
        String providerCode,
        Instant receivedAt,
        List<SportsbookEvent> events,
        ProviderQuota quota,
        String rawPayload
) {

    public SportsbookOddsBatch {
        Objects.requireNonNull(providerCode);
        Objects.requireNonNull(receivedAt);
        Objects.requireNonNull(events);
        Objects.requireNonNull(quota);
        Objects.requireNonNull(rawPayload);

        events = List.copyOf(events);
    }
}