package com.ufcstudy.settlement.calculation;

import com.ufcstudy.domain.paperbetting.PaperBetResult;

import java.math.BigDecimal;
import java.util.Objects;

public final class PaperBetSettlementCalculator {

    public SettlementAmounts calculate(
            PaperBetResult result,
            BigDecimal stakeUnits,
            BigDecimal decimalOdds
    ) {
        Objects.requireNonNull(result);
        Objects.requireNonNull(stakeUnits);
        Objects.requireNonNull(decimalOdds);

        if (stakeUnits.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Stake must be positive"
            );
        }

        if (decimalOdds.compareTo(BigDecimal.ONE) <= 0) {
            throw new IllegalArgumentException(
                    "Decimal odds must exceed one"
            );
        }

        return switch (result) {
            case WIN -> {
                BigDecimal gross =
                        stakeUnits.multiply(decimalOdds);

                yield new SettlementAmounts(
                        result,
                        gross,
                        gross.subtract(stakeUnits)
                );
            }

            case LOSS -> new SettlementAmounts(
                    result,
                    BigDecimal.ZERO,
                    stakeUnits.negate()
            );

            case VOID -> new SettlementAmounts(
                    result,
                    stakeUnits,
                    BigDecimal.ZERO
            );
        };
    }
}