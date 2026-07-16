package com.ufcstudy.settlement.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record EventSettlementResult(
        UUID eventResolutionId,
        UUID sportingEventId,
        int betsSettled,
        List<UUID> paperBetIds
) {

    public EventSettlementResult {
        Objects.requireNonNull(eventResolutionId);
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(paperBetIds);

        paperBetIds = List.copyOf(paperBetIds);

        if (betsSettled != paperBetIds.size()) {
            throw new IllegalArgumentException(
                    "Settlement count must match paper-bet IDs"
            );
        }
    }
}