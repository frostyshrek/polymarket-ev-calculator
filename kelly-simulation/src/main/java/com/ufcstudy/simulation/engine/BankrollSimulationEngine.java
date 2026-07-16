package com.ufcstudy.simulation.engine;

import com.ufcstudy.simulation.model.SimulationBet;
import com.ufcstudy.simulation.model.SimulationRunResult;
import com.ufcstudy.simulation.staking.StakeSizingStrategy;

import java.util.List;
import java.util.Objects;

public final class BankrollSimulationEngine {

    public SimulationRunResult simulate(
            int runNumber,
            double startingBankroll,
            double ruinThreshold,
            List<SimulationBet> bets,
            StakeSizingStrategy strategy
    ) {
        Objects.requireNonNull(bets);
        Objects.requireNonNull(strategy);

        double bankroll = startingBankroll;
        double maximumBankroll = startingBankroll;
        double minimumBankroll = startingBankroll;
        double peakBankroll = startingBankroll;
        double maximumDrawdownFraction = 0.0;

        int betsPlaced = 0;
        int betsSkipped = 0;

        for (SimulationBet bet : bets) {
            if (bankroll <= ruinThreshold) {
                break;
            }

            double stake = strategy.stake(
                    bankroll,
                    bet
            );

            if (stake <= 0.0) {
                betsSkipped++;
                continue;
            }

            if (!Double.isFinite(stake)
                    || stake > bankroll) {
                throw new IllegalStateException(
                        "Invalid stake generated: " + stake
                );
            }

            if (bet.won()) {
                bankroll += stake
                        * (bet.decimalOdds() - 1.0);
            } else {
                bankroll -= stake;
            }

            betsPlaced++;

            maximumBankroll =
                    Math.max(maximumBankroll, bankroll);

            minimumBankroll =
                    Math.min(minimumBankroll, bankroll);

            peakBankroll =
                    Math.max(peakBankroll, bankroll);

            double drawdown =
                    peakBankroll == 0.0
                            ? 0.0
                            : (peakBankroll - bankroll)
                              / peakBankroll;

            maximumDrawdownFraction =
                    Math.max(
                            maximumDrawdownFraction,
                            drawdown
                    );
        }

        return new SimulationRunResult(
                runNumber,
                startingBankroll,
                bankroll,
                bankroll - startingBankroll,
                maximumBankroll,
                minimumBankroll,
                maximumDrawdownFraction,
                betsPlaced,
                betsSkipped,
                bankroll <= ruinThreshold
        );
    }
}