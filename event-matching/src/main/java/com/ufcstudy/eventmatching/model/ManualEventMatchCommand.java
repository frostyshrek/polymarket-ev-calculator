package com.ufcstudy.eventmatching.model;

import com.ufcstudy.domain.matching.SettlementCompatibility;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ManualEventMatchCommand(
        String canonicalEventName,
        Instant scheduledStartTime,

        UUID sportsbookDataSourceId,
        String sportsbookExternalEventId,
        String sportsbookExternalEventName,
        UUID sportsbookMarketId,

        UUID polymarketDataSourceId,
        String polymarketExternalEventId,
        String polymarketExternalEventName,
        UUID predictionMarketId,

        List<OutcomePair> outcomePairs,

        SettlementCompatibility settlementCompatibility,
        String approvalNotes,
        String approvedBy
) {

    public ManualEventMatchCommand {
        requireText(canonicalEventName, "Canonical event name");
        Objects.requireNonNull(
                scheduledStartTime,
                "Scheduled start time cannot be null"
        );

        Objects.requireNonNull(sportsbookDataSourceId);
        Objects.requireNonNull(sportsbookMarketId);
        Objects.requireNonNull(polymarketDataSourceId);
        Objects.requireNonNull(predictionMarketId);

        requireText(
                sportsbookExternalEventId,
                "Sportsbook external event ID"
        );
        requireText(
                polymarketExternalEventId,
                "Polymarket external event ID"
        );

        Objects.requireNonNull(outcomePairs);
        outcomePairs = List.copyOf(outcomePairs);

        if (outcomePairs.size() != 2) {
            throw new IllegalArgumentException(
                    "A UFC moneyline match must contain exactly two outcome pairs"
            );
        }

        Objects.requireNonNull(settlementCompatibility);
        requireText(approvalNotes, "Approval notes");
        requireText(approvedBy, "Approved by");
    }

    private static void requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(value, fieldName + " cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }
    }
}