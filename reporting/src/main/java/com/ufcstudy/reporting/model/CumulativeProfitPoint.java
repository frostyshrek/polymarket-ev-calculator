package com.ufcstudy.reporting.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record CumulativeProfitPoint(
        long sequenceNumber,
        UUID paperBetId,
        Instant settledAt,
        BigDecimal netProfitUnits,
        BigDecimal cumulativeProfitUnits
) {

    public CumulativeProfitPoint {
        Objects.requireNonNull(paperBetId);
        Objects.requireNonNull(settledAt);
        Objects.requireNonNull(netProfitUnits);
        Objects.requireNonNull(cumulativeProfitUnits);
    }
}