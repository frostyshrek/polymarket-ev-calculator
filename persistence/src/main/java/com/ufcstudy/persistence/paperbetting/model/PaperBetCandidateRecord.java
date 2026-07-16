package com.ufcstudy.persistence.paperbetting.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PaperBetCandidateRecord(
        UUID opportunityId,
        UUID strategyVersionId,
        UUID marketMappingId,
        UUID sportingEventId,

        UUID sportsbookMarketId,
        UUID sportsbookOutcomeId,
        UUID predictionMarketOutcomeId,

        String bookmakerCode,

        Instant decisionTime,
        BigDecimal sportsbookDecimalOdds,
        BigDecimal referenceProbability,
        BigDecimal estimatedExpectedValue,

        String qualificationStatus,
        String mappingStatus,
        String settlementCompatibility
) {

    public PaperBetCandidateRecord {
        Objects.requireNonNull(opportunityId);
        Objects.requireNonNull(strategyVersionId);
        Objects.requireNonNull(marketMappingId);
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(sportsbookMarketId);
        Objects.requireNonNull(sportsbookOutcomeId);
        Objects.requireNonNull(predictionMarketOutcomeId);
        Objects.requireNonNull(bookmakerCode);
        Objects.requireNonNull(decisionTime);
        Objects.requireNonNull(sportsbookDecimalOdds);
        Objects.requireNonNull(referenceProbability);
        Objects.requireNonNull(estimatedExpectedValue);
        Objects.requireNonNull(qualificationStatus);
        Objects.requireNonNull(mappingStatus);
        Objects.requireNonNull(settlementCompatibility);
    }
}