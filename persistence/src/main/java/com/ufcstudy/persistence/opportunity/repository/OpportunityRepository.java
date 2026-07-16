package com.ufcstudy.persistence.opportunity.repository;

import com.ufcstudy.persistence.JdbcTime;
import com.ufcstudy.persistence.opportunity.model.OpportunityInsert;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;

public final class OpportunityRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OpportunityRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void insert(OpportunityInsert opportunity) {
        jdbc.update(
                """
                INSERT INTO ufc_study.opportunity (
                    id,
                    strategy_version_id,
                    market_mapping_id,
                    sportsbook_snapshot_id,
                    opposing_sportsbook_snapshot_id,
                    prediction_snapshot_id,
                    decision_time,
                    reference_probability,
                    probability_method,
                    sportsbook_decimal_odds,
                    raw_implied_probability,
                    sportsbook_no_vig_probability,
                    estimated_ev,
                    expected_profit_per_unit,
                    prediction_market_spread,
                    sportsbook_snapshot_age_seconds,
                    prediction_snapshot_age_seconds,
                    source_gap_seconds,
                    seconds_until_scheduled_start,
                    qualification_status,
                    qualification_reason,
                    rejection_code,
                    calculation_version
                )
                VALUES (
                    :id,
                    :strategyVersionId,
                    :marketMappingId,
                    :sportsbookSnapshotId,
                    :opposingSportsbookSnapshotId,
                    :predictionSnapshotId,
                    :decisionTime,
                    :referenceProbability,
                    :probabilityMethod,
                    :sportsbookDecimalOdds,
                    :rawImpliedProbability,
                    :sportsbookNoVigProbability,
                    :estimatedEv,
                    :expectedProfitPerUnit,
                    :predictionMarketSpread,
                    :sportsbookAge,
                    :predictionAge,
                    :sourceGap,
                    :secondsUntilStart,
                    :qualificationStatus,
                    :qualificationReason,
                    :rejectionCode,
                    :calculationVersion
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", opportunity.id())
                        .addValue(
                                "strategyVersionId",
                                opportunity.strategyVersionId()
                        )
                        .addValue(
                                "marketMappingId",
                                opportunity.marketMappingId()
                        )
                        .addValue(
                                "sportsbookSnapshotId",
                                opportunity.sportsbookSnapshotId()
                        )
                        .addValue(
                                "opposingSportsbookSnapshotId",
                                opportunity
                                        .opposingSportsbookSnapshotId()
                        )
                        .addValue(
                                "predictionSnapshotId",
                                opportunity.predictionSnapshotId()
                        )
                        .addValue(
                                "decisionTime",
                                JdbcTime.from(
                                        opportunity.decisionTime()
                                )
                        )
                        .addValue(
                                "referenceProbability",
                                opportunity.referenceProbability()
                        )
                        .addValue(
                                "probabilityMethod",
                                opportunity.probabilityMethod()
                        )
                        .addValue(
                                "sportsbookDecimalOdds",
                                opportunity.sportsbookDecimalOdds()
                        )
                        .addValue(
                                "rawImpliedProbability",
                                opportunity.rawImpliedProbability()
                        )
                        .addValue(
                                "sportsbookNoVigProbability",
                                opportunity
                                        .sportsbookNoVigProbability()
                        )
                        .addValue(
                                "estimatedEv",
                                opportunity.estimatedExpectedValue()
                        )
                        .addValue(
                                "expectedProfitPerUnit",
                                opportunity.expectedProfitPerUnit()
                        )
                        .addValue(
                                "predictionMarketSpread",
                                opportunity.predictionMarketSpread()
                        )
                        .addValue(
                                "sportsbookAge",
                                opportunity
                                        .sportsbookSnapshotAgeSeconds()
                        )
                        .addValue(
                                "predictionAge",
                                opportunity
                                        .predictionSnapshotAgeSeconds()
                        )
                        .addValue(
                                "sourceGap",
                                opportunity.sourceGapSeconds()
                        )
                        .addValue(
                                "secondsUntilStart",
                                opportunity
                                        .secondsUntilScheduledStart()
                        )
                        .addValue(
                                "qualificationStatus",
                                opportunity.qualificationStatus()
                        )
                        .addValue(
                                "qualificationReason",
                                opportunity.qualificationReason()
                        )
                        .addValue(
                                "rejectionCode",
                                opportunity.rejectionCode()
                        )
                        .addValue(
                                "calculationVersion",
                                opportunity.calculationVersion()
                        )
        );
    }

    public boolean existsForSnapshotPair(
            java.util.UUID strategyVersionId,
            java.util.UUID mappingId,
            java.util.UUID sportsbookSnapshotId,
            java.util.UUID predictionSnapshotId
    ) {
        Boolean exists = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM ufc_study.opportunity
                    WHERE strategy_version_id = :strategyVersionId
                      AND market_mapping_id = :mappingId
                      AND sportsbook_snapshot_id =
                          :sportsbookSnapshotId
                      AND prediction_snapshot_id =
                          :predictionSnapshotId
                )
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "strategyVersionId",
                                strategyVersionId
                        )
                        .addValue("mappingId", mappingId)
                        .addValue(
                                "sportsbookSnapshotId",
                                sportsbookSnapshotId
                        )
                        .addValue(
                                "predictionSnapshotId",
                                predictionSnapshotId
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }
}