package com.ufcstudy.console.mappingreview;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MappingReviewCandidate(
        UUID candidateId,
        UUID sportsbookMarketId,
        String sportsbookMarketName,
        UUID predictionMarketId,
        String predictionMarketName,
        BigDecimal participantScore,
        BigDecimal scheduledTimeScore,
        BigDecimal overallScore,
        String decisionReason,
        List<MappingReviewOutcome> sportsbookOutcomes,
        List<MappingReviewOutcome> predictionOutcomes
) {
}