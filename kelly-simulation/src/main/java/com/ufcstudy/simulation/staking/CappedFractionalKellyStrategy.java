package com.ufcstudy.simulation.staking;

import com.ufcstudy.simulation.model.SimulationBet;

import java.util.Objects;

public final class CappedFractionalKellyStrategy
        implements StakeSizingStrategy {

    private final KellyFractionCalculator calculator;
    private final double kellyMultiplier;
    private final double maximumBankrollFraction;
    private final double minimumStakeUnits;

    public CappedFractionalKellyStrategy(
            KellyFractionCalculator calculator,
            double kellyMultiplier,
            double maximumBankrollFraction,
            double minimumStakeUnits
    ) {
        this.calculator = Objects.requireNonNull(calculator);

        if (!Double.isFinite(kellyMultiplier)
                || kellyMultiplier <= 0.0
                || kellyMultiplier > 1.0) {
            throw new IllegalArgumentException(
                    "Kelly multiplier must be in (0, 1]"
            );
        }

        if (!Double.isFinite(maximumBankrollFraction)
                || maximumBankrollFraction <= 0.0
                || maximumBankrollFraction > 1.0) {
            throw new IllegalArgumentException(
                    "Maximum bankroll fraction must be in (0, 1]"
            );
        }

        if (!Double.isFinite(minimumStakeUnits)
                || minimumStakeUnits < 0.0) {
            throw new IllegalArgumentException(
                    "Minimum stake cannot be negative"
            );
        }

        this.kellyMultiplier = kellyMultiplier;
        this.maximumBankrollFraction =
                maximumBankrollFraction;
        this.minimumStakeUnits = minimumStakeUnits;
    }

    @Override
    public double stake(
            double currentBankroll,
            SimulationBet bet
    ) {
        if (currentBankroll <= 0.0) {
            return 0.0;
        }

        double fullKelly =
                calculator.fullKellyFraction(
                        bet.estimatedProbability(),
                        bet.decimalOdds()
                );

        double requestedFraction =
                fullKelly * kellyMultiplier;

        double cappedFraction =
                Math.min(
                        requestedFraction,
                        maximumBankrollFraction
                );

        double stake =
                currentBankroll * cappedFraction;

        if (stake < minimumStakeUnits) {
            return 0.0;
        }

        return Math.min(
                stake,
                currentBankroll
        );
    }

    @Override
    public String strategyCode() {
        return "KELLY_"
                + kellyMultiplier
                + "_CAP_"
                + maximumBankrollFraction;
    }
}