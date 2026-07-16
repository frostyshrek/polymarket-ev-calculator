package com.ufcstudy.persistence.settlement.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record OpenPaperBetRecord(
        UUID paperBetId,
        UUID sportingEventId,
        UUID selectedParticipantId,
        BigDecimal stakeUnits,
        BigDecimal decimalOdds
) {

    public OpenPaperBetRecord {
        Objects.requireNonNull(paperBetId);
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(selectedParticipantId);
        Objects.requireNonNull(stakeUnits);
        Objects.requireNonNull(decimalOdds);
    }
}