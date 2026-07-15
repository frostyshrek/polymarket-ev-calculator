package com.ufcstudy.odds.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record SportsbookMarket(
        String marketKey,
        Instant lastUpdatedAt,
        List<SportsbookOutcome> outcomes
) {

    public SportsbookMarket {
        Objects.requireNonNull(marketKey);
        Objects.requireNonNull(lastUpdatedAt);
        Objects.requireNonNull(outcomes);

        outcomes = List.copyOf(outcomes);
    }
}