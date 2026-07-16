package com.ufcstudy.reporting.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record PaperBetReportRow(
        UUID paperBetId,
        UUID opportunityId,
        UUID strategyVersionId,
        UUID sportingEventId,
        UUID selectedParticipantId,

        String participantName,
        String bookmakerCode,
        String stakeMethod,

        Instant placedAt,
        Instant settledAt,

        BigDecimal decimalOdds,
        BigDecimal referenceProbability,
        BigDecimal estimatedEv,
        BigDecimal stakeUnits,

        String betStatus,
        String betResult,

        BigDecimal grossReturnUnits,
        BigDecimal netProfitUnits
) {

    public PaperBetReportRow {
        Objects.requireNonNull(paperBetId);
        Objects.requireNonNull(opportunityId);
        Objects.requireNonNull(strategyVersionId);
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(bookmakerCode);
        Objects.requireNonNull(stakeMethod);
        Objects.requireNonNull(placedAt);
        Objects.requireNonNull(decimalOdds);
        Objects.requireNonNull(referenceProbability);
        Objects.requireNonNull(estimatedEv);
        Objects.requireNonNull(stakeUnits);
        Objects.requireNonNull(betStatus);
    }
}