package com.ufcstudy.settlement.calculation;

import com.ufcstudy.domain.paperbetting.PaperBetResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperBetSettlementCalculatorTest {

    private final PaperBetSettlementCalculator calculator =
            new PaperBetSettlementCalculator();

    @Test
    void calculatesWin() {
        var result = calculator.calculate(
                PaperBetResult.WIN,
                new BigDecimal("1.00"),
                new BigDecimal("1.90")
        );

        assertEquals(
                0,
                result.grossReturnUnits()
                        .compareTo(new BigDecimal("1.90"))
        );

        assertEquals(
                0,
                result.netProfitUnits()
                        .compareTo(new BigDecimal("0.90"))
        );
    }

    @Test
    void calculatesLoss() {
        var result = calculator.calculate(
                PaperBetResult.LOSS,
                new BigDecimal("1.00"),
                new BigDecimal("1.90")
        );

        assertEquals(
                0,
                result.grossReturnUnits()
                        .compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                0,
                result.netProfitUnits()
                        .compareTo(new BigDecimal("-1.00"))
        );
    }

    @Test
    void calculatesVoid() {
        var result = calculator.calculate(
                PaperBetResult.VOID,
                new BigDecimal("1.00"),
                new BigDecimal("1.90")
        );

        assertEquals(
                0,
                result.grossReturnUnits()
                        .compareTo(new BigDecimal("1.00"))
        );

        assertEquals(
                0,
                result.netProfitUnits()
                        .compareTo(BigDecimal.ZERO)
        );
    }
}