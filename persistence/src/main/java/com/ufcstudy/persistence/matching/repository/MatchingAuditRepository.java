package com.ufcstudy.persistence.matching.repository;

import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class MatchingAuditRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MatchingAuditRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void recordApproval(
            UUID mappingId,
            String description,
            String performedBy,
            Instant occurredAt
    ) {
        jdbc.update(
                """
                INSERT INTO ufc_study.audit_record (
                    id,
                    entity_type,
                    entity_id,
                    action_type,
                    reason_code,
                    description,
                    performed_by,
                    occurred_at
                )
                VALUES (
                    :id,
                    'MARKET_MAPPING',
                    :entityId,
                    'MANUAL_APPROVAL',
                    'EXACT_EVENT_AND_OUTCOME_MATCH',
                    :description,
                    :performedBy,
                    :occurredAt
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue("entityId", mappingId)
                        .addValue("description", description)
                        .addValue(
                                "performedBy",
                                performedBy
                        )
                        .addValue(
                                "occurredAt",
                                JdbcTime.from(occurredAt)
                        )
        );
    }
}