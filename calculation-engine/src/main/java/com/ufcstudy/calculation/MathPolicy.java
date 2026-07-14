package com.ufcstudy.calculation;

import java.math.MathContext;
import java.math.RoundingMode;

public final class MathPolicy {

    /**
     * Internal calculations use 16 significant digits.
     * Values are rounded for display only at the application boundary.
     */
    public static final MathContext INTERNAL =
            new MathContext(16, RoundingMode.HALF_EVEN);

    private MathPolicy() {
    }
}