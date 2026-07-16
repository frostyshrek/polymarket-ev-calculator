package com.ufcstudy.simulation.engine;

import com.ufcstudy.simulation.model.SimulationRunResult;
import com.ufcstudy.simulation.model.SimulationSummary;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class SimulationSummaryCalculator {

    public SimulationSummary summarize(
            String strategyCode,
            List<SimulationRunResult> runs
    ) {
        Objects.requireNonNull(strategyCode);
        Objects.requireNonNull(runs);

        if (runs.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one run is required"
            );
        }

        List<Double> endingBankrolls =
                runs.stream()
                        .map(
                                SimulationRunResult
                                        ::endingBankroll
                        )
                        .sorted()
                        .toList();

        double meanEnding =
                runs.stream()
                        .mapToDouble(
                                SimulationRunResult
                                        ::endingBankroll
                        )
                        .average()
                        .orElseThrow();

        double meanProfit =
                runs.stream()
                        .mapToDouble(
                                SimulationRunResult
                                        ::netProfit
                        )
                        .average()
                        .orElseThrow();

        double meanDrawdown =
                runs.stream()
                        .mapToDouble(
                                SimulationRunResult
                                        ::maximumDrawdownFraction
                        )
                        .average()
                        .orElseThrow();

        long ruinedRuns =
                runs.stream()
                        .filter(
                                SimulationRunResult::ruined
                        )
                        .count();

        long profitableRuns =
                runs.stream()
                        .filter(
                                run -> run.netProfit() > 0.0
                        )
                        .count();

        return new SimulationSummary(
                strategyCode,
                runs.size(),
                meanEnding,
                percentile(endingBankrolls, 0.50),
                percentile(endingBankrolls, 0.05),
                percentile(endingBankrolls, 0.25),
                percentile(endingBankrolls, 0.75),
                percentile(endingBankrolls, 0.95),
                meanProfit,
                meanDrawdown,
                (double) ruinedRuns / runs.size(),
                (double) profitableRuns / runs.size()
        );
    }

    private double percentile(
            List<Double> sortedValues,
            double percentile
    ) {
        int index = (int) Math.ceil(
                percentile * sortedValues.size()
        ) - 1;

        index = Math.max(
                0,
                Math.min(
                        index,
                        sortedValues.size() - 1
                )
        );

        return sortedValues.get(index);
    }
}