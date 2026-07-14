package com.ufcstudy.calculation;

import com.ufcstudy.domain.value.Probability;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MarketQuoteCalculatorTest {

    private final MarketQuoteCalculator calculator =
            new MarketQuoteCalculator();

    @Test
    void calculatesMidpointAndSpread() {
        MarketQuoteResult result = calculator.calculate(
                Probability.of("0.57"),
                Probability.of("0.61")
        );

        assertEquals(
                0,
                result.midpoint().value().compareTo(
                        new BigDecimal("0.59")
                )
        );

        assertEquals(
                0,
                result.spread().compareTo(
                        new BigDecimal("0.04")
                )
        );
    }

    @Test
    void rejectsCrossedQuote() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(
                        Probability.of("0.62"),
                        Probability.of("0.60")
                )
        );
    }
}