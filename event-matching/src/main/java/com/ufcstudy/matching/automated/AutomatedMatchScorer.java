package com.ufcstudy.eventmatching.automated;

import java.time.Duration;
import java.util.Objects;

public final class AutomatedMatchScorer {

    private static final double PARTICIPANT_WEIGHT = 0.80;
    private static final double TIME_WEIGHT = 0.20;

    private final ParticipantPairScorer participantScorer;
    private final ScheduledTimeScorer timeScorer;

    public AutomatedMatchScorer(
            ParticipantPairScorer participantScorer,
            ScheduledTimeScorer timeScorer
    ) {
        this.participantScorer =
                Objects.requireNonNull(participantScorer);
        this.timeScorer =
                Objects.requireNonNull(timeScorer);
    }

    public AutomatedMatchScore score(
            AutomatedMarketCandidate candidate
    ) {
        double participantScore =
                participantScorer.score(
                        candidate.sportsbookParticipants(),
                        candidate.predictionParticipants()
                );

        double timeScore =
                timeScorer.score(
                        candidate.sportsbookScheduledAt(),
                        candidate.predictionScheduledAt()
                );

        Duration difference =
                timeScorer.difference(
                        candidate.sportsbookScheduledAt(),
                        candidate.predictionScheduledAt()
                );

        double overall =
                participantScore * PARTICIPANT_WEIGHT
                        + timeScore * TIME_WEIGHT;

        return new AutomatedMatchScore(
                participantScore,
                timeScore,
                overall,
                difference
        );
    }
}