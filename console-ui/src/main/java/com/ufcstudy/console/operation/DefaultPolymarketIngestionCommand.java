package com.ufcstudy.console.operation;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class DefaultPolymarketIngestionCommand
        implements PolymarketIngestionCommand {

    private final PolymarketIngestionGateway gateway;
    private final Clock clock;

    public DefaultPolymarketIngestionCommand(
            PolymarketIngestionGateway gateway,
            Clock clock
    ) {
        this.gateway = Objects.requireNonNull(gateway);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PolymarketIngestionResult execute() {
        Instant startedAt = clock.instant();

        try {
            PolymarketGatewayResult gatewayResult =
                    gateway.ingest();

            return new PolymarketIngestionResult(
                    gatewayResult.ingestionRunId(),
                    startedAt,
                    clock.instant(),
                    gatewayResult.marketsReceived(),
                    gatewayResult.orderBooksReceived(),
                    gatewayResult.snapshotsStored(),
                    gatewayResult.rejectedRecords(),
                    true,
                    "Polymarket ingestion completed successfully."
            );
        } catch (RuntimeException exception) {
            return new PolymarketIngestionResult(
                    null,
                    startedAt,
                    clock.instant(),
                    0,
                    0,
                    0,
                    0,
                    false,
                    message(exception)
            );
        }
    }

    private static String message(
            RuntimeException exception
    ) {
        String message = exception.getMessage();

        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }

    public interface PolymarketIngestionGateway {

        PolymarketGatewayResult ingest();
    }

    public record PolymarketGatewayResult(
            UUID ingestionRunId,
            int marketsReceived,
            int orderBooksReceived,
            int snapshotsStored,
            int rejectedRecords
    ) {
    }
}