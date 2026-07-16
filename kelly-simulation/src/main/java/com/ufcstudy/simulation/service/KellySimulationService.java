package com.ufcstudy.simulation.service;

import com.ufcstudy.simulation.engine.BankrollSimulationEngine;
import com.ufcstudy.simulation.engine.BootstrapBetSampler;
import com.ufcstudy.simulation.engine.SimulationSummaryCalculator;
import com.ufcstudy.simulation.model.SimulationBet;
import com.ufcstudy.simulation.model.SimulationConfiguration;
import com.ufcstudy.simulation.model.SimulationRunResult;
import com.ufcstudy.simulation.model.SimulationSummary;
import com.ufcstudy.simulation.staking.StakeSizingStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class KellySimulationService {

    private final BootstrapBetSampler sampler;
    private final BankrollSimulationEngine engine;
    private final SimulationSummaryCalculator summaryCalculator;

    public KellySimulationService(
            BootstrapBetSampler sampler,
            BankrollSimulationEngine engine,
            SimulationSummaryCalculator summaryCalculator
    ) {
        this.sampler = Objects.requireNonNull(sampler);
        this.engine = Objects.requireNonNull(engine);
        this.summaryCalculator =
                Objects.requireNonNull(summaryCalculator);
    }

    public SimulationSummary simulate(
            List<SimulationBet> sourceBets,
            SimulationConfiguration configuration,
            StakeSizingStrategy strategy
    ) {
        Objects.requireNonNull(sourceBets);
        Objects.requireNonNull(configuration);
        Objects.requireNonNull(strategy);

        RandomGenerator random =
                RandomGeneratorFactory
                        .of("L64X128MixRandom")
                        .create(configuration.randomSeed());

        List<SimulationRunResult> results =
                new ArrayList<>(
                        configuration.numberOfRuns()
                );

        for (int runNumber = 0;
             runNumber < configuration.numberOfRuns();
             runNumber++) {

            List<SimulationBet> sampledBets =
                    sampler.sample(
                            sourceBets,
                            configuration.betsPerRun(),
                            configuration
                                    .sampleWithReplacement(),
                            random
                    );

            results.add(
                    engine.simulate(
                            runNumber,
                            configuration.startingBankroll(),
                            configuration.ruinThreshold(),
                            sampledBets,
                            strategy
                    )
            );
        }

        return summaryCalculator.summarize(
                strategy.strategyCode(),
                results
        );
    }
}