package com.ufcstudy.persistence.ingestion.service;

import com.ufcstudy.domain.value.Probability;
import com.ufcstudy.persistence.PersistenceIntegrationSupport;
import com.ufcstudy.persistence.hash.Sha256PayloadHasher;
import com.ufcstudy.persistence.ingestion.repository.IngestionRejectionRepository;
import com.ufcstudy.persistence.ingestion.repository.IngestionRunRepository;
import com.ufcstudy.persistence.ingestion.repository.RawPayloadRepository;
import com.ufcstudy.persistence.market.repository.DeterministicUuidFactory;
import com.ufcstudy.persistence.market.repository.SourceMarketRepository;
import com.ufcstudy.persistence.market.repository.SourceOutcomeRepository;
import com.ufcstudy.persistence.snapshot.repository.PredictionMarketSnapshotRepository;
import com.ufcstudy.polymarket.orderbook.model.OrderBookLevel;
import com.ufcstudy.polymarket.orderbook.model.PolymarketOrderBookSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolymarketOrderBookStorageServiceTest
        extends PersistenceIntegrationSupport {

    @Test
    void storesRawPayloadAndNormalizedSnapshot() {
        var dataSource = dataSource();
        var jdbc = jdbc(dataSource);
        var ids = new DeterministicUuidFactory();

        var service =
                new PolymarketOrderBookStorageService(
                        transactions(dataSource),
                        new IngestionRunRepository(jdbc),
                        new RawPayloadRepository(jdbc),
                        new IngestionRejectionRepository(jdbc),
                        new SourceMarketRepository(jdbc, ids),
                        new SourceOutcomeRepository(jdbc, ids),
                        new PredictionMarketSnapshotRepository(jdbc),
                        new Sha256PayloadHasher(),
                        Clock.fixed(
                                Instant.parse(
                                        "2026-07-16T10:00:01Z"
                                ),
                                ZoneOffset.UTC
                        )
                );

        var snapshot =
                new PolymarketOrderBookSnapshot(
                        "condition-1",
                        "token-a",
                        Instant.parse("2026-07-16T10:00:00Z"),
                        Instant.parse("2026-07-16T10:00:01Z"),
                        "hash-1",
                        List.of(
                                new OrderBookLevel(
                                        Probability.of("0.58"),
                                        new BigDecimal("100")
                                )
                        ),
                        List.of(
                                new OrderBookLevel(
                                        Probability.of("0.61"),
                                        new BigDecimal("150")
                                )
                        ),
                        Probability.of("0.58"),
                        Probability.of("0.61"),
                        Probability.of("0.595"),
                        new BigDecimal("0.03"),
                        new BigDecimal("100"),
                        new BigDecimal("150"),
                        BigDecimal.ONE,
                        new BigDecimal("0.01"),
                        Probability.of("0.59"),
                        false,
                        """
                        {
                          "market":"condition-1",
                          "asset_id":"token-a"
                        }
                        """
                );

        service.store(
                "market-1",
                "Will Fighter A defeat Fighter B?",
                "Fighter A",
                snapshot
        );

        Integer payloadCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ufc_study.raw_source_payload
                WHERE external_reference = 'token-a'
                """,
                java.util.Map.of(),
                Integer.class
        );

        Integer snapshotCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ufc_study.prediction_market_snapshot
                WHERE best_bid = 0.58
                  AND best_ask = 0.61
                  AND midpoint = 0.595
                  AND spread = 0.03
                """,
                java.util.Map.of(),
                Integer.class
        );

        assertEquals(1, payloadCount);
        assertEquals(1, snapshotCount);
    }

    @Test
    void storesRawPayloadButRejectsOneSidedBook() {
        var dataSource = dataSource();
        var jdbc = jdbc(dataSource);
        var ids = new DeterministicUuidFactory();

        var service =
                new PolymarketOrderBookStorageService(
                        transactions(dataSource),
                        new IngestionRunRepository(jdbc),
                        new RawPayloadRepository(jdbc),
                        new IngestionRejectionRepository(jdbc),
                        new SourceMarketRepository(jdbc, ids),
                        new SourceOutcomeRepository(jdbc, ids),
                        new PredictionMarketSnapshotRepository(jdbc),
                        new Sha256PayloadHasher(),
                        Clock.systemUTC()
                );

        var snapshot =
                new PolymarketOrderBookSnapshot(
                        "condition-empty",
                        "token-empty",
                        Instant.now(),
                        Instant.now(),
                        "empty-hash",
                        List.of(),
                        List.of(),
                        null,
                        null,
                        null,
                        null,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ONE,
                        new BigDecimal("0.01"),
                        null,
                        false,
                        """
                        {
                          "market":"condition-empty",
                          "asset_id":"token-empty",
                          "bids":[],
                          "asks":[]
                        }
                        """
                );

        service.store(
                "market-empty",
                "Will Fighter X defeat Fighter Y?",
                "Fighter X",
                snapshot
        );

        Integer payloadCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ufc_study.raw_source_payload
                WHERE external_reference = 'token-empty'
                """,
                java.util.Map.of(),
                Integer.class
        );

        Integer rejectionCount = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ufc_study.ingestion_rejection
                WHERE external_reference = 'token-empty'
                  AND rejection_code = 'EMPTY_ORDER_BOOK'
                """,
                java.util.Map.of(),
                Integer.class
        );

        assertEquals(1, payloadCount);
        assertEquals(1, rejectionCount);
    }
}