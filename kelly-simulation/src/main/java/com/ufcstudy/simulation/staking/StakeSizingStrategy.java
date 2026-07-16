package com.ufcstudy.simulation.staking;

import com.ufcstudy.simulation.model.SimulationBet;

public interface StakeSizingStrategy {

    double stake(
            double currentBankroll,
            SimulationBet bet
    );

    String strategyCode();
}