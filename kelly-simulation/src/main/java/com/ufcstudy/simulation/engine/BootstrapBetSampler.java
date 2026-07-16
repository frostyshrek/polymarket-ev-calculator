package com.ufcstudy.simulation.engine;

import com.ufcstudy.simulation.model.SimulationBet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class BootstrapBetSampler {

    public List<SimulationBet> sample(
            List<SimulationBet> source,
            int sampleSize,
            boolean withReplacement,
            RandomGenerator random
    ) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(random);

        if (source.isEmpty()) {
            throw new IllegalArgumentException(
                    "Simulation source cannot be empty"
            );
        }

        if (sampleSize <= 0) {
            throw new IllegalArgumentException(
                    "Sample size must be positive"
            );
        }

        if (withReplacement) {
            List<SimulationBet> result =
                    new ArrayList<>(sampleSize);

            for (int index = 0;
                 index < sampleSize;
                 index++) {
                result.add(
                        source.get(
                                random.nextInt(source.size())
                        )
                );
            }

            return List.copyOf(result);
        }

        if (sampleSize > source.size()) {
            throw new IllegalArgumentException(
                    "Sample size exceeds source size "
                            + "without replacement"
            );
        }

        List<SimulationBet> shuffled =
                new ArrayList<>(source);

        Collections.shuffle(
                shuffled,
                new java.util.Random(
                        random.nextLong()
                )
        );

        return List.copyOf(
                shuffled.subList(0, sampleSize)
        );
    }
}