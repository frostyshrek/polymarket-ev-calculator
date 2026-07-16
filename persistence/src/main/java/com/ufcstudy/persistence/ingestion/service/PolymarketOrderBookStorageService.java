package com.ufcstudy.persistence.ingestion.service;

import com.ufcstudy.polymarket.orderbook.model.PolymarketOrderBookSnapshot;
import com.ufcstudy.persistence.hash.PayloadHasher;
import com.ufcstudy.persistence.ingestion.model.DataSourceIds;
import com.ufcstudy.persistence.ingestion.model.IngestionRunStatus;
import com.ufcstudy.persistence.ingestion.model.IngestionType;
import com.ufcstudy.persistence.ingestion.repository.IngestionRejectionRepository;
import com.ufcstudy.persistence.ingestion.repository.IngestionRunRepository;
import com.ufcstudy.persistence.ingestion.repository.RawPayloadRepository;
import com.ufcstudy.persistence.market.repository.SourceMarketRepository;
import com.ufcstudy.persistence.market.repository.SourceOutcomeRepository;
import com.ufcstudy.persistence.snapshot.repository.PredictionMarketSnapshotRepository;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public final class PolymarketOrderBookStorageService {

    private final TransactionTemplate transactions;
    private final IngestionRunRepository runRepository;
    private final RawPayloadRepository rawPayloadRepository;
    private final IngestionRejectionRepository rejectionRepository;
    private final SourceMarketRepository marketRepository;
    private final SourceOutcomeRepository outcomeRepository;
    private final PredictionMarketSnapshotRepository snapshotRepository;
    private final PayloadHasher payloadHasher;
    private final Clock clock;

    public PolymarketOrderBookStorageService(
            TransactionTemplate transactions,
            IngestionRunRepository runRepository,
            RawPayloadRepository rawPayloadRepository,
            IngestionRejectionRepository rejectionRepository,
            SourceMarketRepository marketRepository,
            SourceOutcomeRepository outcomeRepository,
            PredictionMarketSnapshotRepository snapshotRepository,
            PayloadHasher payloadHasher,
            Clock clock
    ) {
        this.transactions = Objects.requireNonNull(transactions);
        this.runRepository = Objects.requireNonNull(runRepository);
        this.rawPayloadRepository =
                Objects.requireNonNull(rawPayloadRepository);
        this.rejectionRepository =
                Objects.requireNonNull(rejectionRepository);
        this.marketRepository =
                Objects.requireNonNull(marketRepository);
        this.outcomeRepository =
                Objects.requireNonNull(outcomeRepository);
        this.snapshotRepository =
                Objects.requireNonNull(snapshotRepository);
        this.payloadHasher = Objects.requireNonNull(payloadHasher);
        this.clock = Objects.requireNonNull(clock);
    }

    public UUID store(
            String externalMarketId,
            String marketQuestion,
            String outcomeName,
            PolymarketOrderBookSnapshot snapshot
    ) {
        Objects.requireNonNull(externalMarketId);
        Objects.requireNonNull(marketQuestion);
        Objects.requireNonNull(outcomeName);
        Objects.requireNonNull(snapshot);

        UUID runId = UUID.randomUUID();
        UUID rawPayloadId = UUID.randomUUID();

        return transactions.execute(status -> {
            var startedAt = clock.instant();

            runRepository.insertStarted(
                    runId,
                    DataSourceIds.POLYMARKET_CLOB,
                    IngestionType.PREDICTION_MARKET_BOOK,
                    startedAt
            );

            rawPayloadRepository.insert(
                    rawPayloadId,
                    runId,
                    DataSourceIds.POLYMARKET_CLOB,
                    "ORDER_BOOK",
                    "/book",
                    snapshot.tokenId(),
                    200,
                    snapshot.rawPayload(),
                    payloadHasher.hash(snapshot.rawPayload()),
                    snapshot.sourceTimestamp(),
                    snapshot.receivedAt()
            );

            UUID marketId = marketRepository.upsert(
                    DataSourceIds.POLYMARKET_GAMMA,
                    externalMarketId,
                    marketQuestion,
                    "MONEYLINE",
                    "OPEN",
                    false,
                    snapshot.receivedAt()
            );

            UUID outcomeId = outcomeRepository.upsert(
                    marketId,
                    snapshot.tokenId(),
                    outcomeName,
                    "PARTICIPANT_WIN"
            );

            if (!snapshot.hasTwoSidedBook()) {
                String code;

                if (snapshot.bestBid() == null
                        && snapshot.bestAsk() == null) {
                    code = "EMPTY_ORDER_BOOK";
                } else {
                    code = "ONE_SIDED_ORDER_BOOK";
                }

                rejectionRepository.insert(
                        runId,
                        rawPayloadId,
                        DataSourceIds.POLYMARKET_CLOB,
                        snapshot.tokenId(),
                        code,
                        "A valid two-sided CLOB book was unavailable",
                        snapshot.rawPayload(),
                        snapshot.receivedAt()
                );

                runRepository.complete(
                        runId,
                        IngestionRunStatus.PARTIALLY_SUCCEEDED,
                        1,
                        0,
                        clock.instant(),
                        "No normalized snapshot was created"
                );

                return runId;
            }

            snapshotRepository.insert(
                    runId,
                    rawPayloadId,
                    marketId,
                    outcomeId,
                    snapshot.bestBid().value(),
                    snapshot.bestAsk().value(),
                    snapshot.midpoint().value(),
                    snapshot.spread(),
                    snapshot.lastTradePrice() == null
                            ? null
                            : snapshot.lastTradePrice().value(),
                    snapshot.bidDepth(),
                    snapshot.askDepth(),
                    snapshot.sourceTimestamp(),
                    snapshot.receivedAt()
            );

            runRepository.complete(
                    runId,
                    IngestionRunStatus.SUCCEEDED,
                    1,
                    1,
                    clock.instant(),
                    null
            );

            return runId;
        });
    }
}