package com.ufcstudy.simulation.staking;

import com.ufcstudy.simulation.model.SimulationBet;

public final class FlatStakeStrategy
        implements StakeSizingStrategy {

    private final double stakeUnits;

    public FlatStakeStrategy(double stakeUnits) {
        if (!Double.isFinite(stakeUnits)
                || stakeUnits <= 0.0) {
            throw new IllegalArgumentException(
                    "Flat stake must be positive"
            );
        }

        this.stakeUnits = stakeUnits;
    }

    @Override
    public double stake(
            double currentBankroll,
            SimulationBet bet
    ) {
        if (currentBankroll <= 0.0) {
            return 0.0;
        }

        return Math.min(
                stakeUnits,
                currentBankroll
        );
    }

    @Override
    public String strategyCode() {
        return "FLAT_" + stakeUnits;
    }
}