package com.ufcstudy.persistence.matching.repository;

import com.ufcstudy.domain.matching.MappingStatus;
import com.ufcstudy.domain.matching.SettlementCompatibility;
import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class MarketMappingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MarketMappingRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID insertManualApproval(
            UUID sportingEventId,
            UUID sportsbookMarketId,
            UUID predictionMarketId,
            UUID sportsbookOutcomeId,
            UUID predictionOutcomeId,
            SettlementCompatibility compatibility,
            String approvalNotes,
            String approvedBy,
            Instant approvedAt
    ) {
        UUID id = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO ufc_study.market_mapping (
                    id,
                    sporting_event_id,
                    sportsbook_market_id,
                    prediction_market_id,
                    sportsbook_outcome_id,
                    prediction_market_outcome_id,
                    mapping_status,
                    settlement_compatibility,
                    match_confidence,
                    manually_approved,
                    approval_notes,
                    approved_by,
                    approved_at
                )
                VALUES (
                    :id,
                    :sportingEventId,
                    :sportsbookMarketId,
                    :predictionMarketId,
                    :sportsbookOutcomeId,
                    :predictionOutcomeId,
                    :mappingStatus,
                    :compatibility,
                    NULL,
                    TRUE,
                    :approvalNotes,
                    :approvedBy,
                    :approvedAt
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue(
                                "sportingEventId",
                                sportingEventId
                        )
                        .addValue(
                                "sportsbookMarketId",
                                sportsbookMarketId
                        )
                        .addValue(
                                "predictionMarketId",
                                predictionMarketId
                        )
                        .addValue(
                                "sportsbookOutcomeId",
                                sportsbookOutcomeId
                        )
                        .addValue(
                                "predictionOutcomeId",
                                predictionOutcomeId
                        )
                        .addValue(
                                "mappingStatus",
                                MappingStatus.APPROVED_MANUAL.name()
                        )
                        .addValue(
                                "compatibility",
                                compatibility.name()
                        )
                        .addValue(
                                "approvalNotes",
                                approvalNotes
                        )
                        .addValue(
                                "approvedBy",
                                approvedBy
                        )
                        .addValue(
                                "approvedAt",
                                JdbcTime.from(approvedAt)
                        )
        );

        return id;
    }
}