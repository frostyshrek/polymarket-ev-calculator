package com.ufcstudy.eventmatching.automated.persistence;

import com.ufcstudy.eventmatching.automated.AutomatedMatchStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record AutomatedMatchCandidateInsert(
        UUID id,
        UUID runId,
        UUID sportsbookMarketId,
        UUID predictionMarketId,
        BigDecimal participantScore,
        BigDecimal scheduledTimeScore,
        BigDecimal overallScore,
        Long scheduledTimeDifferenceSeconds,
        AutomatedMatchStatus status,
        String decisionReason,
        String sportsbookParticipantKey,
        String predictionParticipantKey,
        UUID createdMappingId
) {
}