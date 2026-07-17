package com.ufcstudy.simulation.engine;

import com.ufcstudy.simulation.model.SimulationBet;
import com.ufcstudy.simulation.staking.FlatStakeStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class BankrollSimulationEngineTest {

    @Test
    void calculatesFlatStakeBankrollPath() {
        var bets = List.of(
                new SimulationBet(2.00, 0.55, true),
                new SimulationBet(2.00, 0.55, false),
                new SimulationBet(3.00, 0.40, true)
        );

        var result =
                new BankrollSimulationEngine()
                        .simulate(
                                0,
                                100.0,
                                10.0,
                                bets,
                                new FlatStakeStrategy(1.0)
                        );

        assertEquals(
                102.0,
                result.endingBankroll(),
                0.000001
        );

        assertEquals(
                2.0,
                result.netProfit(),
                0.000001
        );

        assertEquals(3, result.betsPlaced());
        assertFalse(result.ruined());
    }
}