package com.ufcstudy.console.mappingreview;

import java.util.UUID;

public record MappingReviewOutcome(
        UUID outcomeId,
        String outcomeName,
        String normalizedOutcomeName
) {
}