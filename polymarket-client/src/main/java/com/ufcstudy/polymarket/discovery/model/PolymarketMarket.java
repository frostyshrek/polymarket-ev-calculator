package com.ufcstudy.polymarket.discovery.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record PolymarketMarket(
        String externalMarketId,
        String conditionId,
        String slug,
        String question,
        String resolutionSource,
        Instant endTime,
        boolean active,
        boolean closed,
        boolean orderBookEnabled,
        List<PolymarketOutcomeToken> outcomes
) {

    public PolymarketMarket {
        Objects.requireNonNull(externalMarketId);
        Objects.requireNonNull(question);
        Objects.requireNonNull(outcomes);

        outcomes = List.copyOf(outcomes);
    }
}