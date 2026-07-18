package com.ufcstudy.console.operation;

import com.ufcstudy.paperbetting.model.PaperBetPlacementRequest;
import com.ufcstudy.paperbetting.model.PaperBetPlacementResult;
import com.ufcstudy.paperbetting.service.PaperBetPlacementService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DefaultQualifiedPaperBetPlacementCommand
        implements QualifiedPaperBetPlacementCommand {

    private final NamedParameterJdbcTemplate jdbc;
    private final PaperBetPlacementService placementService;
    private final Clock clock;

    public DefaultQualifiedPaperBetPlacementCommand(
            NamedParameterJdbcTemplate jdbc,
            PaperBetPlacementService placementService,
            Clock clock
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.placementService =
                Objects.requireNonNull(placementService);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public QualifiedPaperBetPlacementResult execute() {
        Instant startedAt = clock.instant();

        try {
            List<UUID> opportunityIds =
                    findQualifiedOpportunityIds();

            int placed = 0;
            int alreadyPlaced = 0;
            int officialEntryAlreadyExists = 0;
            int failed = 0;

            for (UUID opportunityId : opportunityIds) {
                try {
                    PaperBetPlacementResult result =
                            placementService.place(
                                    new PaperBetPlacementRequest(
                                            opportunityId
                                    )
                            );

                    switch (result.status().name()) {
                        case "PLACED" -> placed++;

                        case "ALREADY_PLACED_FOR_OPPORTUNITY" ->
                                alreadyPlaced++;

                        case "OFFICIAL_ENTRY_ALREADY_EXISTS" ->
                                officialEntryAlreadyExists++;

                        default -> failed++;
                    }
                } catch (RuntimeException exception) {
                    failed++;

                    System.out.printf(
                            "Failed to place paper bet for "
                                    + "opportunity %s: %s%n",
                            opportunityId,
                            errorMessage(exception)
                    );
                }
            }

            String message;

            if (opportunityIds.isEmpty()) {
                message =
                        "No qualified opportunities are "
                                + "available for paper betting.";
            } else {
                message =
                        "Qualified paper-bet placement completed.";
            }

            return new QualifiedPaperBetPlacementResult(
                    startedAt,
                    clock.instant(),
                    opportunityIds.size(),
                    placed,
                    alreadyPlaced,
                    officialEntryAlreadyExists,
                    failed,
                    true,
                    message
            );
        } catch (RuntimeException exception) {
            return new QualifiedPaperBetPlacementResult(
                    startedAt,
                    clock.instant(),
                    0,
                    0,
                    0,
                    0,
                    1,
                    false,
                    errorMessage(exception)
            );
        }
    }

    private List<UUID> findQualifiedOpportunityIds() {
        String sql = """
                SELECT opportunity.id
                FROM ufc_study.opportunity opportunity

                JOIN ufc_study.strategy_version strategy
                  ON strategy.id =
                     opportunity.strategy_version_id

                JOIN ufc_study.market_mapping mapping
                  ON mapping.id =
                     opportunity.market_mapping_id

                WHERE opportunity.qualification_status =
                      'QUALIFIED'

                  AND strategy.strategy_status = 'ACTIVE'

                  AND mapping.mapping_status IN (
                      'APPROVED_AUTOMATIC',
                      'APPROVED_MANUAL'
                  )

                  AND mapping.settlement_compatibility =
                      'EXACT'

                  AND NOT EXISTS (
                      SELECT 1
                      FROM ufc_study.paper_bet bet
                      WHERE bet.opportunity_id =
                            opportunity.id
                  )

                ORDER BY
                    opportunity.decision_time ASC,
                    opportunity.id ASC
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (resultSet, rowNumber) ->
                        resultSet.getObject(
                                "id",
                                UUID.class
                        )
        );
    }

    private static String errorMessage(
            RuntimeException exception
    ) {
        String message = exception.getMessage();

        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}