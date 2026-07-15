package com.ufcstudy.odds.model;

import com.ufcstudy.domain.value.DecimalOdds;

import java.util.Objects;

public record SportsbookOutcome(
        String outcomeName,
        DecimalOdds decimalOdds
) {

    public SportsbookOutcome {
        Objects.requireNonNull(outcomeName);
        Objects.requireNonNull(decimalOdds);

        if (outcomeName.isBlank()) {
            throw new IllegalArgumentException(
                    "Outcome name cannot be blank"
            );
        }
    }
}