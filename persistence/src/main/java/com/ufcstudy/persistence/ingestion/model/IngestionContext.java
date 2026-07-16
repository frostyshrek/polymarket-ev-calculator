package com.ufcstudy.persistence.ingestion.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record IngestionContext(
        UUID ingestionRunId,
        UUID rawPayloadId,
        UUID dataSourceId,
        Instant startedAt,
        Instant receivedAt
) {

    public IngestionContext {
        Objects.requireNonNull(ingestionRunId);
        Objects.requireNonNull(rawPayloadId);
        Objects.requireNonNull(dataSourceId);
        Objects.requireNonNull(startedAt);
        Objects.requireNonNull(receivedAt);
    }
}