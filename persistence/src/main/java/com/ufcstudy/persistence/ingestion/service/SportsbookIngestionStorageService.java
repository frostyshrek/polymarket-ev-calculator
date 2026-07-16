package com.ufcstudy.persistence.ingestion.service;

import com.ufcstudy.odds.model.SportsbookOddsBatch;
import com.ufcstudy.persistence.hash.PayloadHasher;
import com.ufcstudy.persistence.ingestion.model.DataSourceIds;
import com.ufcstudy.persistence.ingestion.model.IngestionRunStatus;
import com.ufcstudy.persistence.ingestion.model.IngestionType;
import com.ufcstudy.persistence.ingestion.repository.IngestionRunRepository;
import com.ufcstudy.persistence.ingestion.repository.RawPayloadRepository;
import com.ufcstudy.persistence.market.repository.SourceMarketRepository;
import com.ufcstudy.persistence.market.repository.SourceOutcomeRepository;
import com.ufcstudy.persistence.snapshot.repository.SportsbookSnapshotRepository;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public final class SportsbookIngestionStorageService {

    private final TransactionTemplate transactions;
    private final IngestionRunRepository runRepository;
    private final RawPayloadRepository rawPayloadRepository;
    private final SourceMarketRepository marketRepository;
    private final SourceOutcomeRepository outcomeRepository;
    private final SportsbookSnapshotRepository snapshotRepository;
    private final PayloadHasher payloadHasher;
    private final Clock clock;

    public SportsbookIngestionStorageService(
            TransactionTemplate transactions,
            IngestionRunRepository runRepository,
            RawPayloadRepository rawPayloadRepository,
            SourceMarketRepository marketRepository,
            SourceOutcomeRepository outcomeRepository,
            SportsbookSnapshotRepository snapshotRepository,
            PayloadHasher payloadHasher,
            Clock clock
    ) {
        this.transactions = Objects.requireNonNull(transactions);
        this.runRepository = Objects.requireNonNull(runRepository);
        this.rawPayloadRepository =
                Objects.requireNonNull(rawPayloadRepository);
        this.marketRepository =
                Objects.requireNonNull(marketRepository);
        this.outcomeRepository =
                Objects.requireNonNull(outcomeRepository);
        this.snapshotRepository =
                Objects.requireNonNull(snapshotRepository);
        this.payloadHasher = Objects.requireNonNull(payloadHasher);
        this.clock = Objects.requireNonNull(clock);
    }

    public UUID store(SportsbookOddsBatch batch) {
        Objects.requireNonNull(batch);

        UUID runId = UUID.randomUUID();
        UUID rawPayloadId = UUID.randomUUID();

        return transactions.execute(status -> {
            var startedAt = clock.instant();

            runRepository.insertStarted(
                    runId,
                    DataSourceIds.THE_ODDS_API,
                    IngestionType.SPORTSBOOK_ODDS,
                    startedAt
            );

            rawPayloadRepository.insert(
                    rawPayloadId,
                    runId,
                    DataSourceIds.THE_ODDS_API,
                    "CURRENT_ODDS",
                    "/v4/sports/mma_mixed_martial_arts/odds",
                    null,
                    200,
                    batch.rawPayload(),
                    payloadHasher.hash(batch.rawPayload()),
                    null,
                    batch.receivedAt()
            );

            AtomicInteger received = new AtomicInteger();
            AtomicInteger persisted = new AtomicInteger();

            batch.events().forEach(event ->
                    event.bookmakers().forEach(bookmaker ->
                            bookmaker.markets().forEach(market -> {
                                if (!"h2h".equals(market.marketKey())) {
                                    return;
                                }

                                String externalMarketId =
                                        event.externalEventId()
                                                + ":"
                                                + bookmaker.bookmakerKey()
                                                + ":"
                                                + market.marketKey();

                                UUID marketId =
                                        marketRepository.upsert(
                                                DataSourceIds.THE_ODDS_API,
                                                externalMarketId,
                                                event.participantA()
                                                        + " vs "
                                                        + event.participantB(),
                                                "MONEYLINE",
                                                "OPEN",
                                                false,
                                                batch.receivedAt()
                                        );

                                market.outcomes().forEach(outcome -> {
                                    received.incrementAndGet();

                                    String externalOutcomeId =
                                            externalMarketId
                                                    + ":"
                                                    + outcome.outcomeName();

                                    UUID outcomeId =
                                            outcomeRepository.upsert(
                                                    marketId,
                                                    externalOutcomeId,
                                                    outcome.outcomeName(),
                                                    "PARTICIPANT_WIN"
                                            );

                                    snapshotRepository.insert(
                                            runId,
                                            rawPayloadId,
                                            marketId,
                                            outcomeId,
                                            bookmaker.bookmakerKey(),
                                            outcome.decimalOdds().value(),
                                            false,
                                            market.lastUpdatedAt(),
                                            batch.receivedAt(),
                                            false
                                    );

                                    persisted.incrementAndGet();
                                });
                            })
                    )
            );

            runRepository.complete(
                    runId,
                    IngestionRunStatus.SUCCEEDED,
                    received.get(),
                    persisted.get(),
                    clock.instant(),
                    null
            );

            return runId;
        });
    }
}