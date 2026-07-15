package com.ufcstudy.polymarket.discovery.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record PolymarketDiscoveryBatch(
        Instant receivedAt,
        List<PolymarketEvent> events,
        String rawPayload,
        int offset,
        int limit
) {

    public PolymarketDiscoveryBatch {
        Objects.requireNonNull(receivedAt);
        Objects.requireNonNull(events);
        Objects.requireNonNull(rawPayload);

        events = List.copyOf(events);
    }

    public boolean mayHaveNextPage() {
        return events.size() == limit;
    }
}