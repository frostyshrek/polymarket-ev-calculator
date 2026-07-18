package com.ufcstudy.console.operation;

import java.time.Instant;
import java.util.UUID;

public record PolymarketIngestionResult(
        UUID ingestionRunId,
        Instant startedAt,
        Instant completedAt,
        int marketsReceived,
        int orderBooksReceived,
        int snapshotsStored,
        int rejectedRecords,
        boolean successful,
        String message
) {
}