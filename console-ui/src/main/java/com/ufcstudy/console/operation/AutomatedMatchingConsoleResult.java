package com.ufcstudy.console.operation;

import java.time.Instant;
import java.util.UUID;

public record AutomatedMatchingConsoleResult(
        UUID runId,
        Instant startedAt,
        Instant completedAt,
        int candidatesEvaluated,
        int autoApproved,
        int reviewRequired,
        int rejected,
        int superseded,
        boolean successful,
        String message
) {
}