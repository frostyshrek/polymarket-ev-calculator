package com.ufcstudy.domain.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * An immutable probability between zero and one, inclusive.
 *
 * Examples:
 * 0.50 represents 50%.
 * 0.625 represents 62.5%.
 */
public record Probability(BigDecimal value) {

    public static final BigDecimal ZERO = BigDecimal.ZERO;
    public static final BigDecimal ONE = BigDecimal.ONE;

    public Probability {
        Objects.requireNonNull(value, "Probability value cannot be null");

        if (value.compareTo(ZERO) < 0 || value.compareTo(ONE) > 0) {
            throw new IllegalArgumentException(
                    "Probability must be between 0 and 1 inclusive: " + value
            );
        }
    }

    public static Probability of(String value) {
        return new Probability(new BigDecimal(value));
    }

    public static Probability of(BigDecimal value) {
        return new Probability(value);
    }

    public static Probability fromPercentage(String percentage) {
        BigDecimal decimal = new BigDecimal(percentage)
                .divide(BigDecimal.valueOf(100));

        return new Probability(decimal);
    }

    public BigDecimal asPercentage(int scale) {
        return value
                .multiply(BigDecimal.valueOf(100))
                .setScale(scale, RoundingMode.HALF_UP);
    }
}