package com.ufcstudy.simulation.model;

public record SimulationRunResult(
        int runNumber,
        double startingBankroll,
        double endingBankroll,
        double netProfit,
        double maximumBankroll,
        double minimumBankroll,
        double maximumDrawdownFraction,
        int betsPlaced,
        int betsSkipped,
        boolean ruined
) {

    public SimulationRunResult {
        if (runNumber < 0) {
            throw new IllegalArgumentException(
                    "Run number cannot be negative"
            );
        }

        if (betsPlaced < 0 || betsSkipped < 0) {
            throw new IllegalArgumentException(
                    "Bet counts cannot be negative"
            );
        }
    }
}