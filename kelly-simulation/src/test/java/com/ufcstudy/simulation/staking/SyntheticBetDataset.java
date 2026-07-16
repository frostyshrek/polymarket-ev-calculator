package com.ufcstudy.simulation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public final class SyntheticBetDataset {

    public List<SimulationBet> generate(
            int count,
            double trueWinProbability,
            double estimatedProbability,
            double decimalOdds,
            long seed
    ) {
        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Count must be positive"
            );
        }

        RandomGenerator random =
                RandomGeneratorFactory
                        .of("L64X128MixRandom")
                        .create(seed);

        List<SimulationBet> bets =
                new ArrayList<>(count);

        for (int index = 0;
             index < count;
             index++) {
            bets.add(
                    new SimulationBet(
                            decimalOdds,
                            estimatedProbability,
                            random.nextDouble()
                                    < trueWinProbability
                    )
            );
        }

        return List.copyOf(bets);
    }
}