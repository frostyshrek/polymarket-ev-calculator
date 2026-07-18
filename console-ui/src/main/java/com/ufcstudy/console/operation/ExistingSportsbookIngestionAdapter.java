package com.ufcstudy.console.operation;

import java.util.Objects;

public class ExistingSportsbookIngestionAdapter
        implements DefaultSportsbookIngestionCommand
        .SportsbookIngestionGateway {

    private final SportsbookIngestionRunner ingestionRunner;
    private final SportsbookOperationProperties properties;

    public ExistingSportsbookIngestionAdapter(
            SportsbookIngestionRunner ingestionRunner,
            SportsbookOperationProperties properties
    ) {
        this.ingestionRunner =
                Objects.requireNonNull(ingestionRunner);

        this.properties =
                Objects.requireNonNull(properties);
    }

    @Override
    public DefaultSportsbookIngestionCommand
            .SportsbookGatewayResult ingest() {

        properties.validateForExecution();

        SportsbookIngestionExecution execution =
                ingestionRunner.run();

        return new DefaultSportsbookIngestionCommand
                .SportsbookGatewayResult(
                        execution.ingestionRunId(),
                        execution.payloadsReceived(),
                        execution.snapshotsStored(),
                        execution.rejectedRecords()
                );
    }

    public interface SportsbookIngestionRunner {

        SportsbookIngestionExecution run();
    }

    public record SportsbookIngestionExecution(
            java.util.UUID ingestionRunId,
            int payloadsReceived,
            int snapshotsStored,
            int rejectedRecords
    ) {
    }
}