package com.ufcstudy.paperbetting.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record PaperBetPlacementResult(
        PaperBetPlacementStatus status,
        UUID paperBetId,
        UUID opportunityId,
        BigDecimal stakeUnits,
        String message
) {

    public PaperBetPlacementResult {
        Objects.requireNonNull(status);
        Objects.requireNonNull(opportunityId);
        Objects.requireNonNull(message);

        if (message.isBlank()) {
            throw new IllegalArgumentException(
                    "Message cannot be blank"
            );
        }
    }

    public static PaperBetPlacementResult placed(
            UUID paperBetId,
            UUID opportunityId,
            BigDecimal stakeUnits
    ) {
        return new PaperBetPlacementResult(
                PaperBetPlacementStatus.PLACED,
                paperBetId,
                opportunityId,
                stakeUnits,
                "Paper bet placed"
        );
    }

    public static PaperBetPlacementResult alreadyPlaced(
            UUID opportunityId
    ) {
        return new PaperBetPlacementResult(
                PaperBetPlacementStatus
                        .ALREADY_PLACED_FOR_OPPORTUNITY,
                null,
                opportunityId,
                null,
                "This opportunity already has a paper bet"
        );
    }

    public static PaperBetPlacementResult officialEntryExists(
            UUID opportunityId
    ) {
        return new PaperBetPlacementResult(
                PaperBetPlacementStatus
                        .OFFICIAL_ENTRY_ALREADY_EXISTS,
                null,
                opportunityId,
                null,
                "An earlier official paper bet already exists"
        );
    }
}