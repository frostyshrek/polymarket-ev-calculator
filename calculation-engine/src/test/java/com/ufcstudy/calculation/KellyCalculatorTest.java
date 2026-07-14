package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.domain.value.Probability;
import com.ufcstudy.domain.value.Units;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KellyCalculatorTest {

    private final KellyCalculator calculator = new KellyCalculator();

    @Test
    void calculatesCappedQuarterKelly() {
        KellyResult result = calculator.calculate(
                Probability.of("0.50"),
                DecimalOdds.of("2.10"),
                Units.of("100"),
                new BigDecimal("0.25"),
                new BigDecimal("0.01")
        );

        // Full Kelly: approximately 4.545%
        assertApproximately(
                new BigDecimal("0.0454545"),
                result.fullKellyFraction(),
                new BigDecimal("0.0000001")
        );

        // Quarter Kelly is approximately 1.136%, then capped at 1%.
        assertEquals(
                0,
                result.cappedKellyFraction().compareTo(
                        new BigDecimal("0.01")
                )
        );

        assertEquals(
                0,
                result.stakeUnits().compareTo(
                        new BigDecimal("1.00")
                )
        );
    }

    @Test
    void returnsZeroStakeForNegativeExpectedValue() {
        KellyResult result = calculator.calculate(
                Probability.of("0.50"),
                DecimalOdds.of("1.90"),
                Units.of("100"),
                new BigDecimal("0.25"),
                new BigDecimal("0.01")
        );

        assertEquals(0, result.fullKellyFraction().signum());
        assertEquals(0, result.stakeUnits().signum());
    }

    private static void assertApproximately(
            BigDecimal expected,
            BigDecimal actual,
            BigDecimal tolerance
    ) {
        BigDecimal difference = expected.subtract(actual).abs();

        assertEquals(true, difference.compareTo(tolerance) <= 0);
    }
}