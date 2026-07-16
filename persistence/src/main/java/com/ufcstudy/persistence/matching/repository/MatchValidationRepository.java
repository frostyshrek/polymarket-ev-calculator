package com.ufcstudy.persistence.matching.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class MatchValidationRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public MatchValidationRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public boolean isEligibleMoneylineMarket(UUID marketId) {
        Boolean result = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM ufc_study.source_market
                    WHERE id = :marketId
                      AND market_type = 'MONEYLINE'
                      AND is_live = FALSE
                )
                """,
                new MapSqlParameterSource()
                        .addValue("marketId", marketId),
                Boolean.class
        );

        return Boolean.TRUE.equals(result);
    }

    public boolean outcomeBelongsToMarket(
            UUID outcomeId,
            UUID marketId
    ) {
        Boolean result = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM ufc_study.source_market_outcome
                    WHERE id = :outcomeId
                      AND source_market_id = :marketId
                      AND outcome_type = 'PARTICIPANT_WIN'
                )
                """,
                new MapSqlParameterSource()
                        .addValue("outcomeId", outcomeId)
                        .addValue("marketId", marketId),
                Boolean.class
        );

        return Boolean.TRUE.equals(result);
    }
}