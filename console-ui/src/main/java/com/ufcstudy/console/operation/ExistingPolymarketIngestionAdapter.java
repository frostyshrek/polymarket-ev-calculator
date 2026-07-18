package com.ufcstudy.console.operation;

import java.util.Objects;
import java.util.UUID;

public class ExistingPolymarketIngestionAdapter
        implements DefaultPolymarketIngestionCommand
        .PolymarketIngestionGateway {

    private final PolymarketIngestionRunner ingestionRunner;
    private final PolymarketOperationProperties properties;

    public ExistingPolymarketIngestionAdapter(
            PolymarketIngestionRunner ingestionRunner,
            PolymarketOperationProperties properties
    ) {
        this.ingestionRunner =
                Objects.requireNonNull(ingestionRunner);

        this.properties =
                Objects.requireNonNull(properties);
    }

    @Override
    public DefaultPolymarketIngestionCommand
            .PolymarketGatewayResult ingest() {

        properties.validateForExecution();

        PolymarketIngestionExecution execution =
                ingestionRunner.run(
                        new PolymarketIngestionRequest(
                                properties.eventSlug(),
                                properties.gammaBaseUrl(),
                                properties.clobBaseUrl()
                        )
                );

        return new DefaultPolymarketIngestionCommand
                .PolymarketGatewayResult(
                        execution.ingestionRunId(),
                        execution.marketsReceived(),
                        execution.orderBooksReceived(),
                        execution.snapshotsStored(),
                        execution.rejectedRecords()
                );
    }

    public interface PolymarketIngestionRunner {

        PolymarketIngestionExecution run(
                PolymarketIngestionRequest request
        );
    }

    public record PolymarketIngestionRequest(
            String eventSlug,
            String gammaBaseUrl,
            String clobBaseUrl
    ) {
    }

    public record PolymarketIngestionExecution(
            UUID ingestionRunId,
            int marketsReceived,
            int orderBooksReceived,
            int snapshotsStored,
            int rejectedRecords
    ) {
    }
}