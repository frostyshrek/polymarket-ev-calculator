package com.ufcstudy.persistence.settlement.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record EventResolutionInsert(
        UUID id,
        UUID sportingEventId,
        UUID winningParticipantId,
        String officialResultType,
        String officialResultText,
        UUID resultSourceId,
        String sourceExternalResultId,
        Instant officialResultAt,
        Instant observedAt,
        boolean finalResult,
        String metadataJson
) {

    public EventResolutionInsert {
        Objects.requireNonNull(id);
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(officialResultType);
        Objects.requireNonNull(observedAt);
        Objects.requireNonNull(metadataJson);
    }
}