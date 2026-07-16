package com.ufcstudy.strategy.model;

import java.util.Objects;
import java.util.UUID;

public record OpportunityEvaluationRequest(
        UUID marketMappingId,
        UUID sportsbookSnapshotId,
        UUID predictionSnapshotId
) {

    public OpportunityEvaluationRequest {
        Objects.requireNonNull(marketMappingId);
        Objects.requireNonNull(sportsbookSnapshotId);
        Objects.requireNonNull(predictionSnapshotId);
    }
}