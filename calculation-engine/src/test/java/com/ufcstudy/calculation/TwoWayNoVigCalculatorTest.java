package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.DecimalOdds;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TwoWayNoVigCalculatorTest {

    private final TwoWayNoVigCalculator calculator =
            new TwoWayNoVigCalculator();

    @Test
    void calculatesProportionalNoVigProbabilities() {
        TwoWayNoVigResult result = calculator.calculate(
                DecimalOdds.of("1.70"),
                DecimalOdds.of("2.20")
        );

        assertApproximately(
                new BigDecimal("0.5641"),
                result.noVigProbabilityA().value(),
                new BigDecimal("0.0001")
        );

        assertApproximately(
                new BigDecimal("0.4359"),
                result.noVigProbabilityB().value(),
                new BigDecimal("0.0001")
        );

        assertApproximately(
                BigDecimal.ONE,
                result.noVigProbabilityA().value()
                        .add(result.noVigProbabilityB().value()),
                new BigDecimal("0.0000001")
        );
    }

    private static void assertApproximately(
            BigDecimal expected,
            BigDecimal actual,
            BigDecimal tolerance
    ) {
        BigDecimal difference = expected.subtract(actual).abs();

        assertEquals(
                true,
                difference.compareTo(tolerance) <= 0,
                () -> "Expected " + expected
                        + " but received " + actual
        );
    }
}