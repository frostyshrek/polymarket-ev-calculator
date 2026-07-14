package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.domain.value.Probability;
import com.ufcstudy.domain.value.Units;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ExpectedValueCalculatorTest {

    private final ExpectedValueCalculator calculator =
            new ExpectedValueCalculator();

    @Test
    void calculatesFivePercentExpectedValue() {
        ExpectedValueResult result = calculator.calculate(
                Probability.of("0.50"),
                DecimalOdds.of("2.10"),
                Units.of("100")
        );

        assertEquals(
                0,
                result.expectedReturnRate().compareTo(
                        new BigDecimal("0.050")
                )
        );

        assertEquals(
                0,
                result.expectedProfitUnits().compareTo(
                        new BigDecimal("5.000")
                )
        );

        assertTrue(result.isPositive());
        assertTrue(result.meetsThreshold(new BigDecimal("0.05")));
    }

    @Test
    void calculatesNegativeExpectedValue() {
        ExpectedValueResult result = calculator.calculate(
                Probability.of("0.55"),
                DecimalOdds.of("1.80"),
                Units.of("100")
        );

        assertEquals(
                0,
                result.expectedReturnRate().compareTo(
                        new BigDecimal("-0.010")
                )
        );

        assertEquals(
                0,
                result.expectedProfitUnits().compareTo(
                        new BigDecimal("-1.000")
                )
        );

        assertFalse(result.isPositive());
    }
}