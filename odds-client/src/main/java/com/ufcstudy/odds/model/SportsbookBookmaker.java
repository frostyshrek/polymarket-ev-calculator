package com.ufcstudy.odds.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record SportsbookBookmaker(
        String bookmakerKey,
        String displayName,
        Instant lastUpdatedAt,
        List<SportsbookMarket> markets
) {

    public SportsbookBookmaker {
        Objects.requireNonNull(bookmakerKey);
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(lastUpdatedAt);
        Objects.requireNonNull(markets);

        markets = List.copyOf(markets);
    }
}