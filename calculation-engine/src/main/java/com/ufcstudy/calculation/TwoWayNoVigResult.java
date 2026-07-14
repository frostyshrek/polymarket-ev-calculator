package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.util.Objects;

public record TwoWayNoVigResult(
        Probability rawProbabilityA,
        Probability rawProbabilityB,
        Probability noVigProbabilityA,
        Probability noVigProbabilityB,
        BigDecimal overround
) {

    public TwoWayNoVigResult {
        Objects.requireNonNull(rawProbabilityA);
        Objects.requireNonNull(rawProbabilityB);
        Objects.requireNonNull(noVigProbabilityA);
        Objects.requireNonNull(noVigProbabilityB);
        Objects.requireNonNull(overround);
    }
}