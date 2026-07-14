package com.ufcstudy.calculation;

import java.math.BigDecimal;
import java.util.Objects;

public record KellyResult(
        BigDecimal fullKellyFraction,
        BigDecimal fractionalKellyFraction,
        BigDecimal cappedKellyFraction,
        BigDecimal stakeUnits
) {

    public KellyResult {
        Objects.requireNonNull(fullKellyFraction);
        Objects.requireNonNull(fractionalKellyFraction);
        Objects.requireNonNull(cappedKellyFraction);
        Objects.requireNonNull(stakeUnits);
    }
}