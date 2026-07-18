package com.ufcstudy.console.operation;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class DefaultSportsbookIngestionCommand
        implements SportsbookIngestionCommand {

    private final SportsbookIngestionGateway gateway;
    private final Clock clock;

    public DefaultSportsbookIngestionCommand(
            SportsbookIngestionGateway gateway,
            Clock clock
    ) {
        this.gateway = Objects.requireNonNull(gateway);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public SportsbookIngestionResult execute() {
        Instant startedAt = clock.instant();

        try {
            SportsbookGatewayResult gatewayResult =
                    gateway.ingest();

            return new SportsbookIngestionResult(
                    gatewayResult.ingestionRunId(),
                    startedAt,
                    clock.instant(),
                    gatewayResult.payloadsReceived(),
                    gatewayResult.snapshotsStored(),
                    gatewayResult.rejectedRecords(),
                    true,
                    "Sportsbook ingestion completed successfully."
            );
        } catch (RuntimeException exception) {
            return new SportsbookIngestionResult(
                    null,
                    startedAt,
                    clock.instant(),
                    0,
                    0,
                    0,
                    false,
                    exception.getMessage() == null
                            ? exception.getClass().getSimpleName()
                            : exception.getMessage()
            );
        }
    }

    public interface SportsbookIngestionGateway {

        SportsbookGatewayResult ingest();
    }

    public record SportsbookGatewayResult(
            UUID ingestionRunId,
            int payloadsReceived,
            int snapshotsStored,
            int rejectedRecords
    ) {
    }
}