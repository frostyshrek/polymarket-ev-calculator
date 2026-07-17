package com.ufcstudy.eventmatching.automated;

import java.time.Duration;
import java.util.Objects;

public final class AutomatedMatchDecisionPolicy {

    private static final double AUTO_APPROVE_SCORE = 0.95;
    private static final double REVIEW_SCORE = 0.75;

    private static final double MINIMUM_AUTO_PARTICIPANT_SCORE =
            0.97;

    private static final Duration MAXIMUM_AUTO_TIME_DIFFERENCE =
            Duration.ofHours(12);

    public AutomatedMatchDecision decide(
            AutomatedMarketCandidate candidate,
            AutomatedMatchScore score
    ) {
        Objects.requireNonNull(candidate);
        Objects.requireNonNull(score);

        boolean hasBothTimes =
                candidate.sportsbookScheduledAt() != null
                        && candidate.predictionScheduledAt()
                        != null;

        if (score.overallScore() >= AUTO_APPROVE_SCORE
                && score.participantScore()
                >= MINIMUM_AUTO_PARTICIPANT_SCORE
                && hasBothTimes
                && score.scheduledTimeDifference()
                .compareTo(
                        MAXIMUM_AUTO_TIME_DIFFERENCE
                ) <= 0) {
            return new AutomatedMatchDecision(
                    AutomatedMatchStatus.AUTO_APPROVED,
                    "HIGH_CONFIDENCE_PARTICIPANTS_AND_TIME"
            );
        }

        if (score.overallScore() >= REVIEW_SCORE) {
            return new AutomatedMatchDecision(
                    AutomatedMatchStatus.REVIEW_REQUIRED,
                    hasBothTimes
                            ? "POSSIBLE_MATCH_REQUIRES_REVIEW"
                            : "MISSING_SCHEDULED_TIME"
            );
        }

        return new AutomatedMatchDecision(
                AutomatedMatchStatus.REJECTED,
                "MATCH_SCORE_BELOW_THRESHOLD"
        );
    }
}