package com.ufcstudy.persistence.market.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.ufcstudy.persistence.JdbcTime;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SourceMarketRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final DeterministicUuidFactory uuidFactory;

    public SourceMarketRepository(
            NamedParameterJdbcTemplate jdbc,
            DeterministicUuidFactory uuidFactory
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.uuidFactory = Objects.requireNonNull(uuidFactory);
    }

    public UUID upsert(
            UUID dataSourceId,
            String externalMarketId,
            String marketName,
            String marketType,
            String marketStatus,
            boolean live,
            Instant observedAt
    ) {
        UUID id = uuidFactory.from(
                dataSourceId.toString(),
                externalMarketId
        );

        jdbc.update(
                """
                INSERT INTO ufc_study.source_market (
                    id,
                    data_source_id,
                    external_market_id,
                    market_type,
                    market_name,
                    market_status,
                    is_live,
                    first_seen_at,
                    last_seen_at
                )
                VALUES (
                    :id,
                    :dataSourceId,
                    :externalMarketId,
                    :marketType,
                    :marketName,
                    :marketStatus,
                    :live,
                    :observedAt,
                    :observedAt
                )
                ON CONFLICT (
                    data_source_id,
                    external_market_id
                )
                DO UPDATE SET
                    market_name = EXCLUDED.market_name,
                    market_status = EXCLUDED.market_status,
                    is_live = EXCLUDED.is_live,
                    last_seen_at = GREATEST(
                        ufc_study.source_market.last_seen_at,
                        EXCLUDED.last_seen_at
                    )
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("dataSourceId", dataSourceId)
                        .addValue(
                                "externalMarketId",
                                externalMarketId
                        )
                        .addValue("marketType", marketType)
                        .addValue("marketName", marketName)
                        .addValue("marketStatus", marketStatus)
                        .addValue("live", live)
                        .addValue(
                            "observedAt",
                            JdbcTime.from(observedAt)
                        )
        );

        return id;
    }
}