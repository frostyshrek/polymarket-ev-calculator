package com.ufcstudy.simulation.model;

public record SimulationConfiguration(
        double startingBankroll,
        int numberOfRuns,
        int betsPerRun,
        long randomSeed,
        double ruinThreshold,
        boolean sampleWithReplacement
) {

    public SimulationConfiguration {
        if (!Double.isFinite(startingBankroll)
                || startingBankroll <= 0.0) {
            throw new IllegalArgumentException(
                    "Starting bankroll must be positive"
            );
        }

        if (numberOfRuns <= 0) {
            throw new IllegalArgumentException(
                    "Number of runs must be positive"
            );
        }

        if (betsPerRun <= 0) {
            throw new IllegalArgumentException(
                    "Bets per run must be positive"
            );
        }

        if (!Double.isFinite(ruinThreshold)
                || ruinThreshold < 0.0
                || ruinThreshold >= startingBankroll) {
            throw new IllegalArgumentException(
                    "Ruin threshold must be at least zero "
                            + "and below starting bankroll"
            );
        }
    }
}