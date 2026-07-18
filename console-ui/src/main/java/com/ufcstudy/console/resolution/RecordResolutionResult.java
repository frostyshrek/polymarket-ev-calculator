package com.ufcstudy.console.resolution;

import java.util.UUID;

public record RecordResolutionResult(
        boolean successful,
        UUID resolutionId,
        String message
) {

    public static RecordResolutionResult success(
            UUID resolutionId
    ) {
        return new RecordResolutionResult(
                true,
                resolutionId,
                "Final event resolution recorded."
        );
    }

    public static RecordResolutionResult failure(
            String message
    ) {
        return new RecordResolutionResult(
                false,
                null,
                message
        );
    }
}