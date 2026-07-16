package com.ufcstudy.simulation.model;

public record SimulationBet(
        double decimalOdds,
        double estimatedProbability,
        boolean won
) {

    public SimulationBet {
        if (!Double.isFinite(decimalOdds)
                || decimalOdds <= 1.0) {
            throw new IllegalArgumentException(
                    "Decimal odds must be greater than one"
            );
        }

        if (!Double.isFinite(estimatedProbability)
                || estimatedProbability <= 0.0
                || estimatedProbability >= 1.0) {
            throw new IllegalArgumentException(
                    "Estimated probability must be between zero and one"
            );
        }
    }

    public double estimatedEv() {
        return estimatedProbability * decimalOdds - 1.0;
    }
}