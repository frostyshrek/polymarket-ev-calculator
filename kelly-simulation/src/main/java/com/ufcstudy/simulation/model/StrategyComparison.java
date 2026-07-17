package com.ufcstudy.simulation.model;

import java.util.List;
import java.util.Objects;

public record StrategyComparison(
        SimulationConfiguration configuration,
        List<SimulationSummary> strategies
) {

    public StrategyComparison {
        Objects.requireNonNull(configuration);
        Objects.requireNonNull(strategies);

        strategies = List.copyOf(strategies);
    }
}