package com.ufcstudy.reporting.model;

import java.time.Instant;
import java.util.UUID;

public record ReportFilter(
        UUID strategyVersionId,
        String bookmakerCode,
        String stakeMethod,
        Instant placedFrom,
        Instant placedTo
) {

    public ReportFilter {
        if (bookmakerCode != null
                && bookmakerCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Bookmaker code cannot be blank"
            );
        }

        if (stakeMethod != null
                && stakeMethod.isBlank()) {
            throw new IllegalArgumentException(
                    "Stake method cannot be blank"
            );
        }

        if (placedFrom != null
                && placedTo != null
                && placedFrom.isAfter(placedTo)) {
            throw new IllegalArgumentException(
                    "Report start must not be after report end"
            );
        }
    }

    public static ReportFilter all() {
        return new ReportFilter(
                null,
                null,
                null,
                null,
                null
        );
    }
}