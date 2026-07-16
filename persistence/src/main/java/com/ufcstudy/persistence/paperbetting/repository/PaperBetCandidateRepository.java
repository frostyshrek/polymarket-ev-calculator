package com.ufcstudy.persistence.paperbetting.repository;

import com.ufcstudy.persistence.paperbetting.model.PaperBetCandidateRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PaperBetCandidateRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PaperBetCandidateRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public Optional<PaperBetCandidateRecord> findByOpportunityId(
            UUID opportunityId
    ) {
        var results = jdbc.query(
                """
                SELECT
                    opportunity.id AS opportunity_id,
                    opportunity.strategy_version_id,
                    opportunity.market_mapping_id,
                    mapping.sporting_event_id,

                    mapping.sportsbook_market_id,
                    mapping.sportsbook_outcome_id,
                    mapping.prediction_market_outcome_id,

                    sportsbook_snapshot.bookmaker_code,

                    opportunity.decision_time,
                    opportunity.sportsbook_decimal_odds,
                    opportunity.reference_probability,
                    opportunity.estimated_ev,
                    opportunity.qualification_status,

                    mapping.mapping_status,
                    mapping.settlement_compatibility
                FROM ufc_study.opportunity opportunity
                JOIN ufc_study.market_mapping mapping
                  ON mapping.id =
                     opportunity.market_mapping_id
                JOIN ufc_study.sportsbook_odds_snapshot sportsbook_snapshot
                  ON sportsbook_snapshot.id =
                     opportunity.sportsbook_snapshot_id
                WHERE opportunity.id = :opportunityId
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "opportunityId",
                                opportunityId
                        ),
                (resultSet, rowNumber) ->
                        new PaperBetCandidateRecord(
                                resultSet.getObject(
                                        "opportunity_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "strategy_version_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "market_mapping_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "sporting_event_id",
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
                                resultSet.getObject(
                                        "decision_time",
                                        OffsetDateTime.class
                                ).toInstant(),
                                resultSet.getBigDecimal(
                                        "sportsbook_decimal_odds"
                                ),
                                resultSet.getBigDecimal(
                                        "reference_probability"
                                ),
                                resultSet.getBigDecimal(
                                        "estimated_ev"
                                ),
                                resultSet.getString(
                                        "qualification_status"
                                ),
                                resultSet.getString(
                                        "mapping_status"
                                ),
                                resultSet.getString(
                                        "settlement_compatibility"
                                )
                        )
        );

        return results.stream().findFirst();
    }
}