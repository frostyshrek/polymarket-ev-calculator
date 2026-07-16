package com.ufcstudy.simulation.staking;

public final class KellyFractionCalculator {

    public double fullKellyFraction(
            double probability,
            double decimalOdds
    ) {
        if (!Double.isFinite(probability)
                || probability <= 0.0
                || probability >= 1.0) {
            throw new IllegalArgumentException(
                    "Probability must be between zero and one"
            );
        }

        if (!Double.isFinite(decimalOdds)
                || decimalOdds <= 1.0) {
            throw new IllegalArgumentException(
                    "Decimal odds must be greater than one"
            );
        }

        double fraction =
                (probability * decimalOdds - 1.0)
                        / (decimalOdds - 1.0);

        return Math.max(0.0, fraction);
    }
}