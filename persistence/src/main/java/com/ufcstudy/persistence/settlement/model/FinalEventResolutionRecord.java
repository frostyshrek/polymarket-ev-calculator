package com.ufcstudy.persistence.settlement.model;

import java.time.Instant;
import java.util.UUID;

public record FinalEventResolutionRecord(
        UUID resolutionId,
        UUID sportingEventId,
        UUID winningParticipantId,
        String officialResultType,
        String officialResultText,
        UUID resultSourceId,
        String sourceExternalResultId,
        Instant officialResultAt,
        Instant observedAt
) {
}