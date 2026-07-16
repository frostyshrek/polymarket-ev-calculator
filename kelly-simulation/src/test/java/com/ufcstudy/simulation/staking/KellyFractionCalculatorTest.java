package com.ufcstudy.simulation.staking;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KellyFractionCalculatorTest {

    private final KellyFractionCalculator calculator =
            new KellyFractionCalculator();

    @Test
    void calculatesFullKellyFraction() {
        double fraction =
                calculator.fullKellyFraction(
                        0.60,
                        2.00
                );

        assertEquals(
                0.20,
                fraction,
                0.000001
        );
    }

    @Test
    void negativeKellyIsFlooredAtZero() {
        double fraction =
                calculator.fullKellyFraction(
                        0.40,
                        2.00
                );

        assertEquals(
                0.0,
                fraction,
                0.000001
        );
    }
}