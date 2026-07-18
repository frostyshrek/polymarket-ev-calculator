package com.ufcstudy.console.mappingreview;

import java.util.List;
import java.util.UUID;

public record MappingReviewResult(
        boolean successful,
        String message,
        List<UUID> createdMappingIds
) {

    public static MappingReviewResult success(
            String message,
            List<UUID> createdMappingIds
    ) {
        return new MappingReviewResult(
                true,
                message,
                List.copyOf(createdMappingIds)
        );
    }

    public static MappingReviewResult failure(
            String message
    ) {
        return new MappingReviewResult(
                false,
                message,
                List.of()
        );
    }
}