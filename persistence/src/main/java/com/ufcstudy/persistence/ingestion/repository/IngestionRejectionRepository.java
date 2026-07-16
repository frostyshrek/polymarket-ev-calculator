package com.ufcstudy.persistence.ingestion.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.ufcstudy.persistence.JdbcTime;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class IngestionRejectionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public IngestionRejectionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void insert(
            UUID ingestionRunId,
            UUID rawPayloadId,
            UUID dataSourceId,
            String externalReference,
            String rejectionCode,
            String rejectionReason,
            String rejectedRecordJson,
            Instant occurredAt
    ) {
        jdbc.update(
                """
                INSERT INTO ufc_study.ingestion_rejection (
                    id,
                    ingestion_run_id,
                    raw_payload_id,
                    data_source_id,
                    external_reference,
                    rejection_code,
                    rejection_reason,
                    rejected_record,
                    occurred_at
                )
                VALUES (
                    :id,
                    :ingestionRunId,
                    :rawPayloadId,
                    :dataSourceId,
                    :externalReference,
                    :rejectionCode,
                    :rejectionReason,
                    CAST(:rejectedRecord AS JSONB),
                    :occurredAt
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue(
                                "ingestionRunId",
                                ingestionRunId
                        )
                        .addValue("rawPayloadId", rawPayloadId)
                        .addValue("dataSourceId", dataSourceId)
                        .addValue(
                                "externalReference",
                                externalReference
                        )
                        .addValue(
                                "rejectionCode",
                                rejectionCode
                        )
                        .addValue(
                                "rejectionReason",
                                rejectionReason
                        )
                        .addValue(
                                "rejectedRecord",
                                rejectedRecordJson == null
                                        ? "{}"
                                        : rejectedRecordJson
                        )
                        .addValue(
                                "occurredAt",
                                JdbcTime.from(occurredAt)
                        )
        );
    }
}