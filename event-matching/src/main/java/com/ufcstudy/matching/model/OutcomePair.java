package com.ufcstudy.matching.model;

import java.util.Objects;
import java.util.UUID;

public record OutcomePair(
        FighterSpecification fighter,
        UUID sportsbookOutcomeId,
        UUID predictionMarketOutcomeId
) {

    public OutcomePair {
        Objects.requireNonNull(fighter);
        Objects.requireNonNull(sportsbookOutcomeId);
        Objects.requireNonNull(predictionMarketOutcomeId);
    }
}