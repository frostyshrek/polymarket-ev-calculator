package com.ufcstudy.odds.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record SportsbookEvent(
        String providerCode,
        String externalEventId,
        String sportKey,
        String sportTitle,
        Instant scheduledStartTime,
        String participantA,
        String participantB,
        List<SportsbookBookmaker> bookmakers
) {

    public SportsbookEvent {
        Objects.requireNonNull(providerCode);
        Objects.requireNonNull(externalEventId);
        Objects.requireNonNull(sportKey);
        Objects.requireNonNull(scheduledStartTime);
        Objects.requireNonNull(participantA);
        Objects.requireNonNull(participantB);
        Objects.requireNonNull(bookmakers);

        bookmakers = List.copyOf(bookmakers);
    }
}