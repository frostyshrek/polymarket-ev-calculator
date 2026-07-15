package com.ufcstudy.polymarket.discovery.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record PolymarketEvent(
        String externalEventId,
        String slug,
        String title,
        String description,
        Instant startTime,
        Instant endTime,
        boolean active,
        boolean closed,
        Set<String> tags,
        List<PolymarketMarket> markets
) {

    public PolymarketEvent {
        Objects.requireNonNull(externalEventId);
        Objects.requireNonNull(title);
        Objects.requireNonNull(tags);
        Objects.requireNonNull(markets);

        tags = Set.copyOf(tags);
        markets = List.copyOf(markets);
    }
}