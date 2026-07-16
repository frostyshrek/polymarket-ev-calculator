package com.ufcstudy.settlement.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record OpenPaperBet(
        UUID paperBetId,
        UUID sportingEventId,
        UUID selectedParticipantId,
        BigDecimal stakeUnits,
        BigDecimal decimalOdds
) {

    public OpenPaperBet {
        Objects.requireNonNull(paperBetId);
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(selectedParticipantId);
        Objects.requireNonNull(stakeUnits);
        Objects.requireNonNull(decimalOdds);
    }
}