package com.ufcstudy.eventmatching.automated;

import java.util.UUID;

public record AutomatedMatchingResult(
        UUID runId,
        int candidatesEvaluated,
        int autoApproved,
        int reviewRequired,
        int rejected,
        int superseded
) {
}