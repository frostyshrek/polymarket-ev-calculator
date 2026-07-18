package com.ufcstudy.console.resolution;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ResolvableEvent(
        UUID eventId,
        String eventName,
        Instant scheduledStartTime,
        String eventStatus,
        int openPaperBetCount,
        List<ResolvableParticipant> participants
) {
}