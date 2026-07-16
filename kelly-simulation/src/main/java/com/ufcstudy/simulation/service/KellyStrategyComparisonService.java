package com.ufcstudy.simulation.service;

import com.ufcstudy.simulation.model.SimulationBet;
import com.ufcstudy.simulation.model.SimulationConfiguration;
import com.ufcstudy.simulation.model.StrategyComparison;
import com.ufcstudy.simulation.staking.StakeSizingStrategy;

import java.util.List;
import java.util.Objects;

public final class KellyStrategyComparisonService {

    private final KellySimulationService simulationService;

    public KellyStrategyComparisonService(
            KellySimulationService simulationService
    ) {
        this.simulationService =
                Objects.requireNonNull(simulationService);
    }

    public StrategyComparison compare(
            List<SimulationBet> sourceBets,
            SimulationConfiguration configuration,
            List<StakeSizingStrategy> strategies
    ) {
        Objects.requireNonNull(sourceBets);
        Objects.requireNonNull(configuration);
        Objects.requireNonNull(strategies);

        var summaries =
                strategies.stream()
                        .map(
                                strategy ->
                                        simulationService
                                                .simulate(
                                                        sourceBets,
                                                        configuration,
                                                        strategy
                                                )
                        )
                        .toList();

        return new StrategyComparison(
                configuration,
                summaries
        );
    }
}