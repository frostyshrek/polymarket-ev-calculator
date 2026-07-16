package com.ufcstudy.eventmatching.automated.persistence;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class ExistingMarketMappingRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ExistingMarketMappingRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public boolean mappingExists(
            UUID sportsbookMarketId,
            UUID predictionMarketId
    ) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ufc_study.market_mapping
                WHERE sportsbook_market_id =
                      :sportsbookMarketId
                   OR prediction_market_id =
                      :predictionMarketId
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "sportsbookMarketId",
                                sportsbookMarketId
                        )
                        .addValue(
                                "predictionMarketId",
                                predictionMarketId
                        ),
                Integer.class
        );

        return count != null && count > 0;
    }
}