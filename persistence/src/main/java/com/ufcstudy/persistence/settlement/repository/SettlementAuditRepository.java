package com.ufcstudy.persistence.settlement.repository;

import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SettlementAuditRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SettlementAuditRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void recordPaperBetSettlement(
            UUID paperBetId,
            String result,
            BigDecimal grossReturnUnits,
            BigDecimal netProfitUnits,
            String performedBy,
            Instant occurredAt
    ) {
        String description =
                "Settled paper bet as "
                        + result
                        + "; gross return="
                        + grossReturnUnits
                        + "; net profit="
                        + netProfitUnits;

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
                    'PAPER_BET',
                    :paperBetId,
                    'SETTLEMENT',
                    :result,
                    :description,
                    :performedBy,
                    :occurredAt
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue(
                                "paperBetId",
                                paperBetId
                        )
                        .addValue("result", result)
                        .addValue(
                                "description",
                                description
                        )
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

    public void recordEventResolution(
            UUID resolutionId,
            UUID sportingEventId,
            String resultType,
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
                    'EVENT_RESOLUTION',
                    :resolutionId,
                    'FINAL_RESOLUTION',
                    :resultType,
                    :description,
                    :performedBy,
                    :occurredAt
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue(
                                "resolutionId",
                                resolutionId
                        )
                        .addValue(
                                "resultType",
                                resultType
                        )
                        .addValue(
                                "description",
                                "Finalized event "
                                        + sportingEventId
                                        + " as "
                                        + resultType
                        )
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