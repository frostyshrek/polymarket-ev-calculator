package com.ufcstudy.eventmatching.automated;

import java.util.List;
import java.util.Objects;

public final class ParticipantPairScorer {

    private final FighterNameSimilarity similarity;

    public ParticipantPairScorer(
            FighterNameSimilarity similarity
    ) {
        this.similarity = Objects.requireNonNull(similarity);
    }

    public double score(
            ParticipantPair sportsbook,
            ParticipantPair prediction
    ) {
        List<String> left = sportsbook.participantNames();
        List<String> right = prediction.participantNames();

        double direct =
                average(
                        similarity.similarity(
                                left.get(0),
                                right.get(0)
                        ),
                        similarity.similarity(
                                left.get(1),
                                right.get(1)
                        )
                );

        double reversed =
                average(
                        similarity.similarity(
                                left.get(0),
                                right.get(1)
                        ),
                        similarity.similarity(
                                left.get(1),
                                right.get(0)
                        )
                );

        return Math.max(direct, reversed);
    }

    private double average(
            double first,
            double second
    ) {
        return (first + second) / 2.0;
    }
}