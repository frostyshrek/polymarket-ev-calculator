package com.ufcstudy.simulation.staking;

import com.ufcstudy.simulation.model.SimulationBet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CappedFractionalKellyStrategyTest {

    @Test
    void quarterKellyIsCappedAtTwoPercent() {
        var strategy =
                new CappedFractionalKellyStrategy(
                        new KellyFractionCalculator(),
                        0.25,
                        0.02,
                        0.01
                );

        var bet =
                new SimulationBet(
                        2.00,
                        0.70,
                        true
                );

        double stake =
                strategy.stake(
                        100.0,
                        bet
                );

        assertEquals(
                2.0,
                stake,
                0.000001
        );
    }

    @Test
    void negativeEvBetProducesNoStake() {
        var strategy =
                new CappedFractionalKellyStrategy(
                        new KellyFractionCalculator(),
                        0.25,
                        0.02,
                        0.01
                );

        var bet =
                new SimulationBet(
                        2.00,
                        0.40,
                        false
                );

        assertEquals(
                0.0,
                strategy.stake(100.0, bet),
                0.000001
        );
    }
}