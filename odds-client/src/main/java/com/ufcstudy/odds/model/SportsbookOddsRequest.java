package com.ufcstudy.odds.model;

import java.util.List;
import java.util.Objects;

public record SportsbookOddsRequest(
        String sportKey,
        List<String> regions,
        List<String> markets,
        OddsFormat oddsFormat
) {

    public SportsbookOddsRequest {
        Objects.requireNonNull(sportKey);
        Objects.requireNonNull(regions);
        Objects.requireNonNull(markets);
        Objects.requireNonNull(oddsFormat);

        regions = List.copyOf(regions);
        markets = List.copyOf(markets);

        if (sportKey.isBlank()) {
            throw new IllegalArgumentException("Sport key cannot be blank");
        }

        if (regions.isEmpty()) {
            throw new IllegalArgumentException("Regions cannot be empty");
        }

        if (markets.isEmpty()) {
            throw new IllegalArgumentException("Markets cannot be empty");
        }
    }
}