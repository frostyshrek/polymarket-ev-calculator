package com.ufcstudy.simulation.service;

import com.ufcstudy.simulation.engine.BankrollSimulationEngine;
import com.ufcstudy.simulation.engine.BootstrapBetSampler;
import com.ufcstudy.simulation.engine.SimulationSummaryCalculator;
import com.ufcstudy.simulation.model.SimulationBet;
import com.ufcstudy.simulation.model.SimulationConfiguration;
import com.ufcstudy.simulation.staking.FlatStakeStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KellySimulationServiceTest {

    @Test
    void fixedSeedProducesRepeatableSimulation() {
        var service =
                new KellySimulationService(
                        new BootstrapBetSampler(),
                        new BankrollSimulationEngine(),
                        new SimulationSummaryCalculator()
                );

        var bets = List.of(
                new SimulationBet(2.00, 0.55, true),
                new SimulationBet(1.80, 0.60, false),
                new SimulationBet(2.40, 0.45, true)
        );

        var configuration =
                new SimulationConfiguration(
                        100.0,
                        100,
                        50,
                        42L,
                        10.0,
                        true
                );

        var first =
                service.simulate(
                        bets,
                        configuration,
                        new FlatStakeStrategy(1.0)
                );

        var second =
                service.simulate(
                        bets,
                        configuration,
                        new FlatStakeStrategy(1.0)
                );

        assertEquals(
                first.meanEndingBankroll(),
                second.meanEndingBankroll(),
                0.000001
        );

        assertEquals(
                first.ruinProbability(),
                second.ruinProbability(),
                0.000001
        );
    }
}