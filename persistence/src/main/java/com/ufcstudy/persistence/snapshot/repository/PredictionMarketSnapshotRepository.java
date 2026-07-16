package com.ufcstudy.persistence.snapshot.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.ufcstudy.persistence.JdbcTime;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PredictionMarketSnapshotRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PredictionMarketSnapshotRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID insert(
            UUID ingestionRunId,
            UUID rawPayloadId,
            UUID sourceMarketId,
            UUID sourceOutcomeId,
            BigDecimal bestBid,
            BigDecimal bestAsk,
            BigDecimal midpoint,
            BigDecimal spread,
            BigDecimal lastTradePrice,
            BigDecimal bidDepth,
            BigDecimal askDepth,
            Instant sourceUpdatedAt,
            Instant observedAt
    ) {
        UUID id = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO ufc_study.prediction_market_snapshot (
                    id,
                    ingestion_run_id,
                    raw_payload_id,
                    source_market_id,
                    source_outcome_id,
                    best_bid,
                    best_ask,
                    midpoint,
                    spread,
                    last_trade_price,
                    bid_depth,
                    ask_depth,
                    source_updated_at,
                    observed_at
                )
                VALUES (
                    :id,
                    :ingestionRunId,
                    :rawPayloadId,
                    :sourceMarketId,
                    :sourceOutcomeId,
                    :bestBid,
                    :bestAsk,
                    :midpoint,
                    :spread,
                    :lastTradePrice,
                    :bidDepth,
                    :askDepth,
                    :sourceUpdatedAt,
                    :observedAt
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
                        .addValue("bestBid", bestBid)
                        .addValue("bestAsk", bestAsk)
                        .addValue("midpoint", midpoint)
                        .addValue("spread", spread)
                        .addValue(
                                "lastTradePrice",
                                lastTradePrice
                        )
                        .addValue("bidDepth", bidDepth)
                        .addValue("askDepth", askDepth)
                        .addValue(
                                "sourceUpdatedAt",
                                JdbcTime.from(sourceUpdatedAt)
                        )
                        .addValue(
                                "observedAt",
                                JdbcTime.from(observedAt)
                        )
        );

        return id;
    }
}