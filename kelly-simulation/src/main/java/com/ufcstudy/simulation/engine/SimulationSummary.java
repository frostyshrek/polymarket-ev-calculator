package com.ufcstudy.simulation.model;

public record SimulationSummary(
        String strategyCode,
        int numberOfRuns,

        double meanEndingBankroll,
        double medianEndingBankroll,

        double percentile05EndingBankroll,
        double percentile25EndingBankroll,
        double percentile75EndingBankroll,
        double percentile95EndingBankroll,

        double meanNetProfit,
        double meanMaximumDrawdownFraction,

        double ruinProbability,
        double profitableRunProbability
) {
}