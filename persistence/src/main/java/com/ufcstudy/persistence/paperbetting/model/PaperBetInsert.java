package com.ufcstudy.persistence.paperbetting.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PaperBetInsert(
        UUID id,
        UUID strategyVersionId,
        UUID opportunityId,
        UUID sportingEventId,
        UUID marketMappingId,

        UUID sportsbookMarketId,
        UUID sportsbookOutcomeId,
        UUID predictionMarketOutcomeId,

        String bookmakerCode,

        Instant placedAt,
        BigDecimal decimalOdds,
        BigDecimal referenceProbability,
        BigDecimal estimatedExpectedValue,

        String stakeMethod,
        BigDecimal stakeUnits,
        String betStatus
) {

    public PaperBetInsert {
        Objects.requireNonNull(id);
        Objects.requireNonNull(strategyVersionId);
        Objects.requireNonNull(opportunityId);
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(marketMappingId);
        Objects.requireNonNull(sportsbookMarketId);
        Objects.requireNonNull(sportsbookOutcomeId);
        Objects.requireNonNull(predictionMarketOutcomeId);
        Objects.requireNonNull(bookmakerCode);
        Objects.requireNonNull(placedAt);
        Objects.requireNonNull(decimalOdds);
        Objects.requireNonNull(referenceProbability);
        Objects.requireNonNull(estimatedExpectedValue);
        Objects.requireNonNull(stakeMethod);
        Objects.requireNonNull(stakeUnits);
        Objects.requireNonNull(betStatus);
    }
}