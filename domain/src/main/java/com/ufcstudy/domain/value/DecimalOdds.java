package com.ufcstudy.domain.value;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Decimal sportsbook odds.
 *
 * Odds must be greater than 1.00 for the initial UFC moneyline study.
 */
public record DecimalOdds(BigDecimal value) {

    private static final BigDecimal MINIMUM_EXCLUSIVE = BigDecimal.ONE;

    public DecimalOdds {
        Objects.requireNonNull(value, "Decimal odds cannot be null");

        if (value.compareTo(MINIMUM_EXCLUSIVE) <= 0) {
            throw new IllegalArgumentException(
                    "Decimal odds must be greater than 1.00: " + value
            );
        }
    }

    public static DecimalOdds of(String value) {
        return new DecimalOdds(new BigDecimal(value));
    }

    public static DecimalOdds of(BigDecimal value) {
        return new DecimalOdds(value);
    }

    public BigDecimal netProfitMultiplier() {
        return value.subtract(BigDecimal.ONE);
    }
}