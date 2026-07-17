package com.ufcstudy.eventmatching.automated;

import org.junit.jupiter.api.Test;

import com.ufcstudy.eventmatching.automated.AutomatedMarketCandidate;
import com.ufcstudy.eventmatching.automated.AutomatedMatchDecision;
import com.ufcstudy.eventmatching.automated.AutomatedMatchDecisionPolicy;
import com.ufcstudy.eventmatching.automated.AutomatedMatchScorer;
import com.ufcstudy.eventmatching.automated.AutomatedMatchStatus;
import com.ufcstudy.eventmatching.automated.FighterNameNormalizer;
import com.ufcstudy.eventmatching.automated.FighterNameSimilarity;
import com.ufcstudy.eventmatching.automated.ParticipantPair;
import com.ufcstudy.eventmatching.automated.ParticipantPairScorer;
import com.ufcstudy.eventmatching.automated.ScheduledTimeScorer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutomatedMatchDecisionPolicyTest {

    @Test
    void approvesExactParticipantsAndCloseTime() {
        FighterNameNormalizer normalizer =
                new FighterNameNormalizer();

        AutomatedMatchScorer scorer =
                new AutomatedMatchScorer(
                        new ParticipantPairScorer(
                                new FighterNameSimilarity(
                                        normalizer
                                )
                        ),
                        new ScheduledTimeScorer()
                );

        Instant start =
                Instant.parse("2026-08-01T22:00:00Z");

        AutomatedMarketCandidate candidate =
                new AutomatedMarketCandidate(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        new ParticipantPair(
                                List.of(
                                        "Fighter One",
                                        "Fighter Two"
                                )
                        ),
                        new ParticipantPair(
                                List.of(
                                        "Fighter Two",
                                        "Fighter One"
                                )
                        ),
                        start,
                        start.plusSeconds(3600)
                );

        AutomatedMatchDecision decision =
                new AutomatedMatchDecisionPolicy()
                        .decide(
                                candidate,
                                scorer.score(candidate)
                        );

        assertEquals(
                AutomatedMatchStatus.AUTO_APPROVED,
                decision.status()
        );
    }
}