package com.ufcstudy.paperbetting.model;

import java.util.Objects;
import java.util.UUID;

public record PaperBetPlacementRequest(
        UUID opportunityId
) {

    public PaperBetPlacementRequest {
        Objects.requireNonNull(
                opportunityId,
                "Opportunity ID cannot be null"
        );
    }
}