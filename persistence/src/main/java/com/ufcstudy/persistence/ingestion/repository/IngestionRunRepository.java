package com.ufcstudy.persistence.ingestion.repository;

import com.ufcstudy.persistence.ingestion.model.IngestionRunStatus;
import com.ufcstudy.persistence.ingestion.model.IngestionType;
import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class IngestionRunRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public IngestionRunRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void insertStarted(
            UUID runId,
            UUID dataSourceId,
            IngestionType ingestionType,
            Instant startedAt
    ) {
        jdbc.update(
                """
                INSERT INTO ufc_study.ingestion_run (
                    id,
                    data_source_id,
                    ingestion_type,
                    started_at,
                    run_status
                )
                VALUES (
                    :id,
                    :dataSourceId,
                    :ingestionType,
                    :startedAt,
                    'STARTED'
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", runId)
                        .addValue("dataSourceId", dataSourceId)
                        .addValue(
                                "ingestionType",
                                ingestionType.name()
                        )
                        .addValue("startedAt", JdbcTime.from(startedAt))
        );
    }

    public void complete(
            UUID runId,
            IngestionRunStatus status,
            int recordsReceived,
            int recordsPersisted,
            Instant completedAt,
            String errorMessage
    ) {
        jdbc.update(
                """
                UPDATE ufc_study.ingestion_run
                SET completed_at = :completedAt,
                    run_status = :status,
                    records_received = :recordsReceived,
                    records_persisted = :recordsPersisted,
                    error_message = :errorMessage
                WHERE id = :id
                """,
                new MapSqlParameterSource()
                        .addValue("id", runId)
                        .addValue("completedAt", JdbcTime.from(completedAt))
                        .addValue("status", status.name())
                        .addValue(
                                "recordsReceived",
                                recordsReceived
                        )
                        .addValue(
                                "recordsPersisted",
                                recordsPersisted
                        )
                        .addValue("errorMessage", errorMessage)
        );
    }
}