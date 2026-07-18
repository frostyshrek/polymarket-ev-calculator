package com.ufcstudy.console.settlement;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CompletedEventSettlementResult(
        Instant startedAt,
        Instant completedAt,
        int eligibleEvents,
        int eventsSettled,
        int betsSettled,
        int failedEvents,
        List<UUID> settledEventIds,
        boolean successful,
        String message
) {

    public CompletedEventSettlementResult {
        settledEventIds =
                List.copyOf(settledEventIds);
    }
}