package com.ufcstudy.eventmatching.automated;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record AutomatedMarketCandidate(
        UUID sportsbookMarketId,
        UUID predictionMarketId,
        ParticipantPair sportsbookParticipants,
        ParticipantPair predictionParticipants,
        Instant sportsbookScheduledAt,
        Instant predictionScheduledAt
) {

    public AutomatedMarketCandidate {
        Objects.requireNonNull(sportsbookMarketId);
        Objects.requireNonNull(predictionMarketId);
        Objects.requireNonNull(sportsbookParticipants);
        Objects.requireNonNull(predictionParticipants);
    }
}