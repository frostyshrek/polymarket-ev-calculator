package com.ufcstudy.console.operation;

import java.time.Instant;
import java.util.UUID;

public record SportsbookIngestionResult(
        UUID ingestionRunId,
        Instant startedAt,
        Instant completedAt,
        int payloadsReceived,
        int snapshotsStored,
        int rejectedRecords,
        boolean successful,
        String message
) {
}