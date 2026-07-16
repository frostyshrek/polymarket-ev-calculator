package com.ufcstudy.persistence.snapshot.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.ufcstudy.persistence.JdbcTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SportsbookSnapshotRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SportsbookSnapshotRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID insert(
            UUID ingestionRunId,
            UUID rawPayloadId,
            UUID sourceMarketId,
            UUID sourceOutcomeId,
            String bookmakerCode,
            BigDecimal decimalOdds,
            boolean live,
            Instant sourceUpdatedAt,
            Instant observedAt,
            boolean suspended
    ) {
        UUID id = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO ufc_study.sportsbook_odds_snapshot (
                    id,
                    ingestion_run_id,
                    raw_payload_id,
                    source_market_id,
                    source_outcome_id,
                    bookmaker_code,
                    decimal_odds,
                    is_live,
                    source_updated_at,
                    observed_at,
                    market_suspended
                )
                VALUES (
                    :id,
                    :ingestionRunId,
                    :rawPayloadId,
                    :sourceMarketId,
                    :sourceOutcomeId,
                    :bookmakerCode,
                    :decimalOdds,
                    :live,
                    :sourceUpdatedAt,
                    :observedAt,
                    :suspended
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue(
                                "ingestionRunId",
                                ingestionRunId
                        )
                        .addValue("rawPayloadId", rawPayloadId)
                        .addValue(
                                "sourceMarketId",
                                sourceMarketId
                        )
                        .addValue(
                                "sourceOutcomeId",
                                sourceOutcomeId
                        )
                        .addValue(
                                "bookmakerCode",
                                bookmakerCode
                        )
                        .addValue("decimalOdds", decimalOdds)
                        .addValue("live", live)
                        .addValue(
                                "sourceUpdatedAt",
                                JdbcTime.from(sourceUpdatedAt)
                        )
                        .addValue(
                                "observedAt",
                                JdbcTime.from(observedAt)
                        )
                        .addValue("suspended", suspended)
        );

        return id;
    }
}