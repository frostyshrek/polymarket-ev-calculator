package com.ufcstudy.persistence.paperbetting.repository;

import com.ufcstudy.persistence.JdbcTime;
import com.ufcstudy.persistence.paperbetting.model.PaperBetInsert;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class PaperBetRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PaperBetRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void insert(PaperBetInsert bet) {
        jdbc.update(
                """
                INSERT INTO ufc_study.paper_bet (
                    id,
                    strategy_version_id,
                    opportunity_id,
                    sporting_event_id,
                    market_mapping_id,
                    sportsbook_market_id,
                    sportsbook_outcome_id,
                    prediction_market_outcome_id,
                    bookmaker_code,
                    placed_at,
                    decimal_odds,
                    reference_probability,
                    estimated_ev,
                    stake_method,
                    stake_units,
                    bet_status
                )
                VALUES (
                    :id,
                    :strategyVersionId,
                    :opportunityId,
                    :sportingEventId,
                    :marketMappingId,
                    :sportsbookMarketId,
                    :sportsbookOutcomeId,
                    :predictionMarketOutcomeId,
                    :bookmakerCode,
                    :placedAt,
                    :decimalOdds,
                    :referenceProbability,
                    :estimatedEv,
                    :stakeMethod,
                    :stakeUnits,
                    :betStatus
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", bet.id())
                        .addValue(
                                "strategyVersionId",
                                bet.strategyVersionId()
                        )
                        .addValue(
                                "opportunityId",
                                bet.opportunityId()
                        )
                        .addValue(
                                "sportingEventId",
                                bet.sportingEventId()
                        )
                        .addValue(
                                "marketMappingId",
                                bet.marketMappingId()
                        )
                        .addValue(
                                "sportsbookMarketId",
                                bet.sportsbookMarketId()
                        )
                        .addValue(
                                "sportsbookOutcomeId",
                                bet.sportsbookOutcomeId()
                        )
                        .addValue(
                                "predictionMarketOutcomeId",
                                bet.predictionMarketOutcomeId()
                        )
                        .addValue(
                                "bookmakerCode",
                                bet.bookmakerCode()
                        )
                        .addValue(
                                "placedAt",
                                JdbcTime.from(bet.placedAt())
                        )
                        .addValue(
                                "decimalOdds",
                                bet.decimalOdds()
                        )
                        .addValue(
                                "referenceProbability",
                                bet.referenceProbability()
                        )
                        .addValue(
                                "estimatedEv",
                                bet.estimatedExpectedValue()
                        )
                        .addValue(
                                "stakeMethod",
                                bet.stakeMethod()
                        )
                        .addValue(
                                "stakeUnits",
                                bet.stakeUnits()
                        )
                        .addValue(
                                "betStatus",
                                bet.betStatus()
                        )
        );
    }

    public boolean existsForOpportunity(UUID opportunityId) {
        Boolean exists = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM ufc_study.paper_bet
                    WHERE opportunity_id = :opportunityId
                )
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "opportunityId",
                                opportunityId
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }

    public boolean officialEntryExists(
            UUID strategyVersionId,
            UUID sportingEventId,
            UUID sportsbookOutcomeId,
            String bookmakerCode,
            String stakeMethod
    ) {
        Boolean exists = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM ufc_study.paper_bet
                    WHERE strategy_version_id =
                          :strategyVersionId
                      AND sporting_event_id =
                          :sportingEventId
                      AND sportsbook_outcome_id =
                          :sportsbookOutcomeId
                      AND bookmaker_code =
                          :bookmakerCode
                      AND stake_method =
                          :stakeMethod
                )
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "strategyVersionId",
                                strategyVersionId
                        )
                        .addValue(
                                "sportingEventId",
                                sportingEventId
                        )
                        .addValue(
                                "sportsbookOutcomeId",
                                sportsbookOutcomeId
                        )
                        .addValue(
                                "bookmakerCode",
                                bookmakerCode
                        )
                        .addValue(
                                "stakeMethod",
                                stakeMethod
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }
}