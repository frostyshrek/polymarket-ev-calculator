package com.ufcstudy.console.settlement;

import com.ufcstudy.settlement.model.EventSettlementResult;
import com.ufcstudy.console.settlement.service.EventSettlementService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DefaultCompletedEventSettlementCommand
        implements CompletedEventSettlementCommand {

    private final NamedParameterJdbcTemplate jdbc;
    private final EventSettlementService settlementService;
    private final Clock clock;

    public DefaultCompletedEventSettlementCommand(
            NamedParameterJdbcTemplate jdbc,
            EventSettlementService settlementService,
            Clock clock
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.settlementService =
                Objects.requireNonNull(settlementService);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public List<SettleableEvent> findEligibleEvents() {
        return jdbc.query(
                """
                SELECT
                    event.id AS sporting_event_id,
                    event.canonical_name,
                    event.scheduled_start_time,
                    resolution.official_result_type,
                    resolution.official_result_text,
                    COUNT(bet.id) AS open_bet_count

                FROM ufc_study.sporting_event event

                JOIN LATERAL (
                    SELECT candidate.*
                    FROM ufc_study.event_resolution candidate
                    WHERE candidate.sporting_event_id =
                          event.id
                      AND candidate.is_final = TRUE
                    ORDER BY
                        candidate.observed_at DESC,
                        candidate.created_at DESC
                    LIMIT 1
                ) resolution ON TRUE

                JOIN ufc_study.paper_bet bet
                  ON bet.sporting_event_id = event.id
                 AND bet.bet_status = 'OPEN'

                GROUP BY
                    event.id,
                    event.canonical_name,
                    event.scheduled_start_time,
                    resolution.official_result_type,
                    resolution.official_result_text,
                    resolution.observed_at

                ORDER BY
                    resolution.observed_at ASC,
                    event.scheduled_start_time ASC,
                    event.id ASC
                """,
                new MapSqlParameterSource(),
                (resultSet, rowNumber) ->
                        mapEvent(resultSet)
        );
    }

    @Override
    public CompletedEventSettlementResult settleAll(
            String resolvedBy
    ) {
        if (resolvedBy == null
                || resolvedBy.isBlank()) {
            return failure(
                    "Resolved by must not be blank."
            );
        }

        Instant startedAt = clock.instant();

        List<SettleableEvent> events =
                findEligibleEvents();

        int eventsSettled = 0;
        int betsSettled = 0;
        int failedEvents = 0;

        List<UUID> settledEventIds =
                new ArrayList<>();

        for (SettleableEvent event : events) {
            try {
                EventSettlementResult result =
                        settlementService
                                .settleRecordedResolution(
                                        event.sportingEventId(),
                                        resolvedBy.trim()
                                );

                eventsSettled++;
                betsSettled += result.betsSettled();

                settledEventIds.add(
                        event.sportingEventId()
                );
            } catch (RuntimeException exception) {
                failedEvents++;

                System.out.printf(
                        "Failed to settle event %s (%s): %s%n",
                        event.eventName(),
                        event.sportingEventId(),
                        errorMessage(exception)
                );
            }
        }

        String message = events.isEmpty()
                ? "No completed events have open paper bets."
                : "Completed-event settlement finished.";

        return new CompletedEventSettlementResult(
                startedAt,
                clock.instant(),
                events.size(),
                eventsSettled,
                betsSettled,
                failedEvents,
                settledEventIds,
                true,
                message
        );
    }

    private CompletedEventSettlementResult failure(
            String message
    ) {
        Instant now = clock.instant();

        return new CompletedEventSettlementResult(
                now,
                now,
                0,
                0,
                0,
                1,
                List.of(),
                false,
                message
        );
    }

    private static SettleableEvent mapEvent(
            ResultSet resultSet
    ) throws SQLException {
        OffsetDateTime scheduledStart =
                resultSet.getObject(
                        "scheduled_start_time",
                        OffsetDateTime.class
                );

        return new SettleableEvent(
                resultSet.getObject(
                        "sporting_event_id",
                        UUID.class
                ),
                resultSet.getString(
                        "canonical_name"
                ),
                scheduledStart == null
                        ? null
                        : scheduledStart.toInstant(),
                resultSet.getString(
                        "official_result_type"
                ),
                resultSet.getString(
                        "official_result_text"
                ),
                resultSet.getInt(
                        "open_bet_count"
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