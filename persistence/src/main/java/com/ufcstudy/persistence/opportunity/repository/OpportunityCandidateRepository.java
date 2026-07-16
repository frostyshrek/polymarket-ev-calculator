package com.ufcstudy.persistence.opportunity.repository;

import com.ufcstudy.persistence.opportunity.model.OpportunityCandidateRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class OpportunityCandidateRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OpportunityCandidateRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public Optional<OpportunityCandidateRecord> findCandidate(
            UUID mappingId,
            UUID sportsbookSnapshotId,
            UUID predictionSnapshotId
    ) {
        var results = jdbc.query(
                """
                SELECT
                    mm.id AS market_mapping_id,
                    mm.sporting_event_id,
                    se.scheduled_start_time,
                    mm.mapping_status,
                    mm.settlement_compatibility,

                    selected.id AS sportsbook_snapshot_id,
                    opposing.id AS opposing_snapshot_id,
                    prediction.id AS prediction_snapshot_id,

                    mm.sportsbook_market_id,
                    mm.sportsbook_outcome_id,
                    mm.prediction_market_outcome_id,

                    selected.bookmaker_code,
                    selected.decimal_odds AS selected_decimal_odds,
                    opposing.decimal_odds AS opposing_decimal_odds,

                    selected.observed_at AS selected_observed_at,
                    opposing.observed_at AS opposing_observed_at,
                    prediction.observed_at AS prediction_observed_at,

                    prediction.best_bid,
                    prediction.best_ask,
                    prediction.midpoint,
                    prediction.spread
                FROM ufc_study.market_mapping mm
                JOIN ufc_study.sporting_event se
                  ON se.id = mm.sporting_event_id

                JOIN ufc_study.sportsbook_odds_snapshot selected
                  ON selected.id = :sportsbookSnapshotId
                 AND selected.source_market_id =
                     mm.sportsbook_market_id
                 AND selected.source_outcome_id =
                     mm.sportsbook_outcome_id

                JOIN ufc_study.sportsbook_odds_snapshot opposing
                  ON opposing.source_market_id =
                     selected.source_market_id
                 AND opposing.bookmaker_code =
                     selected.bookmaker_code
                 AND opposing.source_outcome_id <>
                     selected.source_outcome_id
                 AND opposing.ingestion_run_id =
                     selected.ingestion_run_id

                JOIN ufc_study.prediction_market_snapshot prediction
                  ON prediction.id = :predictionSnapshotId
                 AND prediction.source_market_id =
                     mm.prediction_market_id
                 AND prediction.source_outcome_id =
                     mm.prediction_market_outcome_id

                WHERE mm.id = :mappingId
                ORDER BY opposing.observed_at DESC
                LIMIT 1
                """,
                new MapSqlParameterSource()
                        .addValue("mappingId", mappingId)
                        .addValue(
                                "sportsbookSnapshotId",
                                sportsbookSnapshotId
                        )
                        .addValue(
                                "predictionSnapshotId",
                                predictionSnapshotId
                        ),
                (resultSet, rowNumber) ->
                        new OpportunityCandidateRecord(
                                resultSet.getObject(
                                        "market_mapping_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "sporting_event_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "scheduled_start_time",
                                        java.time.OffsetDateTime.class
                                ).toInstant(),
                                resultSet.getString(
                                        "mapping_status"
                                ),
                                resultSet.getString(
                                        "settlement_compatibility"
                                ),
                                resultSet.getObject(
                                        "sportsbook_snapshot_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "opposing_snapshot_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "prediction_snapshot_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "sportsbook_market_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "sportsbook_outcome_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "prediction_market_outcome_id",
                                        UUID.class
                                ),
                                resultSet.getString(
                                        "bookmaker_code"
                                ),
                                resultSet.getBigDecimal(
                                        "selected_decimal_odds"
                                ),
                                resultSet.getBigDecimal(
                                        "opposing_decimal_odds"
                                ),
                                resultSet.getObject(
                                        "selected_observed_at",
                                        java.time.OffsetDateTime.class
                                ).toInstant(),
                                resultSet.getObject(
                                        "opposing_observed_at",
                                        java.time.OffsetDateTime.class
                                ).toInstant(),
                                resultSet.getObject(
                                        "prediction_observed_at",
                                        java.time.OffsetDateTime.class
                                ).toInstant(),
                                resultSet.getBigDecimal("best_bid"),
                                resultSet.getBigDecimal("best_ask"),
                                resultSet.getBigDecimal("midpoint"),
                                resultSet.getBigDecimal("spread")
                        )
        );

        return results.stream().findFirst();
    }
}