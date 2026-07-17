package com.ufcstudy.eventmatching.automated;

import org.junit.jupiter.api.Test;

import com.ufcstudy.eventmatching.automated.FighterNameNormalizer;
import com.ufcstudy.eventmatching.automated.FighterNameSimilarity;
import com.ufcstudy.eventmatching.automated.ParticipantPair;
import com.ufcstudy.eventmatching.automated.ParticipantPairScorer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParticipantPairScorerTest {

    @Test
    void participantOrderDoesNotAffectScore() {
        FighterNameNormalizer normalizer =
                new FighterNameNormalizer();

        ParticipantPairScorer scorer =
                new ParticipantPairScorer(
                        new FighterNameSimilarity(normalizer)
                );

        ParticipantPair sportsbook =
                new ParticipantPair(
                        List.of(
                                "José Aldo",
                                "Conor McGregor"
                        )
                );

        ParticipantPair prediction =
                new ParticipantPair(
                        List.of(
                                "Conor McGregor",
                                "Jose Aldo"
                        )
                );

        assertEquals(
                1.0,
                scorer.score(
                        sportsbook,
                        prediction
                ),
                0.000001
        );
    }
}