package com.ufcstudy.domain.value;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A non-negative quantity measured in betting units.
 */
public record Units(BigDecimal value) {

    public Units {
        Objects.requireNonNull(value, "Units cannot be null");

        if (value.signum() < 0) {
            throw new IllegalArgumentException(
                    "Units cannot be negative: " + value
            );
        }
    }

    public static Units of(String value) {
        return new Units(new BigDecimal(value));
    }

    public static Units of(BigDecimal value) {
        return new Units(value);
    }

    public static Units zero() {
        return new Units(BigDecimal.ZERO);
    }
}