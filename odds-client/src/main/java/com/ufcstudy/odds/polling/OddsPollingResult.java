package com.ufcstudy.odds.polling;

import com.ufcstudy.odds.model.SportsbookOddsBatch;

public record OddsPollingResult(
        Status status,
        SportsbookOddsBatch batch,
        String reason
) {

    public enum Status {
        COMPLETED,
        SKIPPED_ALREADY_RUNNING,
        SKIPPED_LOW_QUOTA
    }

    public static OddsPollingResult completed(
            SportsbookOddsBatch batch
    ) {
        return new OddsPollingResult(
                Status.COMPLETED,
                batch,
                null
        );
    }

    public static OddsPollingResult skipped(
            Status status,
            String reason
    ) {
        return new OddsPollingResult(status, null, reason);
    }
}