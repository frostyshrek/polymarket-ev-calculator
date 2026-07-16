package com.ufcstudy.persistence.opportunity.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record OpportunityCandidateRecord(
        UUID marketMappingId,
        UUID sportingEventId,
        Instant scheduledStartTime,
        String mappingStatus,
        String settlementCompatibility,

        UUID sportsbookSnapshotId,
        UUID opposingSportsbookSnapshotId,
        UUID predictionSnapshotId,

        UUID sportsbookMarketId,
        UUID sportsbookOutcomeId,
        UUID predictionOutcomeId,

        String bookmakerCode,
        BigDecimal sportsbookDecimalOdds,
        BigDecimal opposingSportsbookDecimalOdds,

        Instant sportsbookObservedAt,
        Instant opposingSportsbookObservedAt,
        Instant predictionObservedAt,

        BigDecimal predictionBestBid,
        BigDecimal predictionBestAsk,
        BigDecimal predictionMidpoint,
        BigDecimal predictionSpread
) {

    public OpportunityCandidateRecord {
        Objects.requireNonNull(marketMappingId);
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(scheduledStartTime);
        Objects.requireNonNull(mappingStatus);
        Objects.requireNonNull(settlementCompatibility);
        Objects.requireNonNull(sportsbookSnapshotId);
        Objects.requireNonNull(opposingSportsbookSnapshotId);
        Objects.requireNonNull(predictionSnapshotId);
        Objects.requireNonNull(sportsbookMarketId);
        Objects.requireNonNull(sportsbookOutcomeId);
        Objects.requireNonNull(predictionOutcomeId);
        Objects.requireNonNull(bookmakerCode);
        Objects.requireNonNull(sportsbookDecimalOdds);
        Objects.requireNonNull(opposingSportsbookDecimalOdds);
        Objects.requireNonNull(sportsbookObservedAt);
        Objects.requireNonNull(opposingSportsbookObservedAt);
        Objects.requireNonNull(predictionObservedAt);
        Objects.requireNonNull(predictionBestBid);
        Objects.requireNonNull(predictionBestAsk);
        Objects.requireNonNull(predictionMidpoint);
        Objects.requireNonNull(predictionSpread);
    }
}