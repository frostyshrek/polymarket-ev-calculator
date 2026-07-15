package com.ufcstudy.polymarket.discovery.model;

import com.ufcstudy.domain.value.Probability;

import java.util.Objects;

public record PolymarketOutcomeToken(
        String outcomeName,
        String tokenId,
        Probability displayedProbability
) {

    public PolymarketOutcomeToken {
        Objects.requireNonNull(outcomeName);
        Objects.requireNonNull(tokenId);

        if (outcomeName.isBlank()) {
            throw new IllegalArgumentException(
                    "Outcome name cannot be blank"
            );
        }

        if (tokenId.isBlank()) {
            throw new IllegalArgumentException(
                    "Token ID cannot be blank"
            );
        }
    }
}