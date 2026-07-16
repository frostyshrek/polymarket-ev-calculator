package com.ufcstudy.persistence.ingestion.repository;

import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class RawPayloadRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public RawPayloadRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void insert(
            UUID id,
            UUID ingestionRunId,
            UUID dataSourceId,
            String endpointName,
            String requestUri,
            String externalReference,
            Integer responseStatus,
            String payload,
            String payloadHash,
            Instant sourceTimestamp,
            Instant receivedAt
    ) {
        jdbc.update(
                """
                INSERT INTO ufc_study.raw_source_payload (
                    id,
                    ingestion_run_id,
                    data_source_id,
                    endpoint_name,
                    request_uri,
                    external_reference,
                    response_status,
                    payload,
                    payload_hash,
                    source_timestamp,
                    received_at
                )
                VALUES (
                    :id,
                    :ingestionRunId,
                    :dataSourceId,
                    :endpointName,
                    :requestUri,
                    :externalReference,
                    :responseStatus,
                    CAST(:payload AS JSONB),
                    :payloadHash,
                    :sourceTimestamp,
                    :receivedAt
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue(
                                "ingestionRunId",
                                ingestionRunId
                        )
                        .addValue("dataSourceId", dataSourceId)
                        .addValue("endpointName", endpointName)
                        .addValue("requestUri", requestUri)
                        .addValue(
                                "externalReference",
                                externalReference
                        )
                        .addValue(
                                "responseStatus",
                                responseStatus
                        )
                        .addValue("payload", payload)
                        .addValue("payloadHash", payloadHash)
                        .addValue(
                                "sourceTimestamp",
                                JdbcTime.from(sourceTimestamp)
                        )
                        .addValue(
                                "receivedAt",
                                JdbcTime.from(receivedAt)
                        )
        );
    }
}