package com.ufcstudy.console.resolution;

import com.ufcstudy.persistence.settlement.model.EventResolutionInsert;
import com.ufcstudy.persistence.settlement.repository.EventResolutionRepository;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EventResolutionRecordingService {

    private final NamedParameterJdbcTemplate jdbc;
    private final EventResolutionRepository resolutionRepository;
    private final TransactionTemplate transactions;
    private final Clock clock;

    public EventResolutionRecordingService(
            NamedParameterJdbcTemplate jdbc,
            EventResolutionRepository resolutionRepository,
            TransactionTemplate transactions,
            Clock clock
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.resolutionRepository =
                Objects.requireNonNull(resolutionRepository);
        this.transactions =
                Objects.requireNonNull(transactions);
        this.clock = Objects.requireNonNull(clock);
    }

    public List<ResolvableEvent> findResolvableEvents() {
        String sql = """
                SELECT
                    event.id,
                    event.canonical_name,
                    event.scheduled_start_time,
                    event.event_status,
                    COUNT(bet.id) AS open_bet_count

                FROM ufc_study.sporting_event event

                JOIN ufc_study.paper_bet bet
                  ON bet.sporting_event_id = event.id
                 AND bet.bet_status = 'OPEN'

                WHERE NOT EXISTS (
                    SELECT 1
                    FROM ufc_study.event_resolution resolution
                    WHERE resolution.sporting_event_id = event.id
                      AND resolution.is_final = TRUE
                )

                GROUP BY
                    event.id,
                    event.canonical_name,
                    event.scheduled_start_time,
                    event.event_status

                ORDER BY
                    event.scheduled_start_time ASC,
                    event.canonical_name ASC
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (resultSet, rowNumber) ->
                        mapEvent(resultSet)
        );
    }

    public RecordResolutionResult recordParticipantWin(
            UUID eventId,
            UUID winningParticipantId,
            String resultText,
            String recordedBy
    ) {
        return record(
                eventId,
                winningParticipantId,
                "PARTICIPANT_WIN",
                resultText,
                recordedBy
        );
    }

    public RecordResolutionResult recordNonWinnerResult(
            UUID eventId,
            String resultType,
            String resultText,
            String recordedBy
    ) {
        if (!List.of(
                "DRAW",
                "NO_CONTEST",
                "CANCELLED",
                "POSTPONED"
        ).contains(resultType)) {
            return RecordResolutionResult.failure(
                    "Unsupported non-winner result type: "
                            + resultType
            );
        }

        return record(
                eventId,
                null,
                resultType,
                resultText,
                recordedBy
        );
    }

    private RecordResolutionResult record(
            UUID eventId,
            UUID winningParticipantId,
            String resultType,
            String resultText,
            String recordedBy
    ) {
        Objects.requireNonNull(eventId);

        String normalizedRecorder =
                requireText(recordedBy, "Recorded by");

        String normalizedResultText =
                requireText(resultText, "Result description");

        try {
            return transactions.execute(status -> {
                validate(
                        eventId,
                        winningParticipantId,
                        resultType
                );

                UUID resolutionId = UUID.randomUUID();
                Instant now = clock.instant();

                resolutionRepository.insert(
                        new EventResolutionInsert(
                                resolutionId,
                                eventId,
                                winningParticipantId,
                                resultType,
                                normalizedResultText,
                                null,
                                null,
                                now,
                                now,
                                true,
                                metadataJson(
                                        normalizedRecorder
                                )
                        )
                );

                updateEventStatus(
                        eventId,
                        resultType
                );

                return RecordResolutionResult.success(
                        resolutionId
                );
            });
        } catch (RuntimeException exception) {
            return RecordResolutionResult.failure(
                    errorMessage(exception)
            );
        }
    }

    private void validate(
            UUID eventId,
            UUID winningParticipantId,
            String resultType
    ) {
        if (!resolutionRepository.sportingEventExists(
                eventId
        )) {
            throw new IllegalArgumentException(
                    "Sporting event does not exist."
            );
        }

        if (resolutionRepository.finalResolutionExists(
                eventId
        )) {
            throw new IllegalStateException(
                    "A final resolution already exists "
                            + "for this event."
            );
        }

        if ("PARTICIPANT_WIN".equals(resultType)) {
            if (winningParticipantId == null) {
                throw new IllegalArgumentException(
                        "A participant win requires "
                                + "a winning participant."
                );
            }

            if (!resolutionRepository
                    .participantBelongsToEvent(
                            eventId,
                            winningParticipantId
                    )) {
                throw new IllegalArgumentException(
                        "The selected winner is not attached "
                                + "to this event."
                );
            }
        } else if (winningParticipantId != null) {
            throw new IllegalArgumentException(
                    "Only PARTICIPANT_WIN may specify "
                            + "a winning participant."
            );
        }
    }

    private void updateEventStatus(
            UUID eventId,
            String resultType
    ) {
        String eventStatus = switch (resultType) {
            case "CANCELLED" -> "CANCELLED";
            case "POSTPONED" -> "POSTPONED";
            default -> "COMPLETED";
        };

        int updated = jdbc.update(
                """
                UPDATE ufc_study.sporting_event
                SET event_status = :eventStatus,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :eventId
                """,
                new MapSqlParameterSource()
                        .addValue("eventId", eventId)
                        .addValue(
                                "eventStatus",
                                eventStatus
                        )
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Could not update sporting event status."
            );
        }
    }

    private ResolvableEvent mapEvent(
            ResultSet resultSet
    ) throws SQLException {
        UUID eventId = resultSet.getObject(
                "id",
                UUID.class
        );

        return new ResolvableEvent(
                eventId,
                resultSet.getString(
                        "canonical_name"
                ),
                toInstant(
                        resultSet.getObject(
                                "scheduled_start_time",
                                OffsetDateTime.class
                        )
                ),
                resultSet.getString(
                        "event_status"
                ),
                resultSet.getInt(
                        "open_bet_count"
                ),
                findParticipants(eventId)
        );
    }

    private List<ResolvableParticipant> findParticipants(
            UUID eventId
    ) {
        return jdbc.query(
                """
                SELECT
                    participant.id,
                    participant.canonical_name,
                    event_participant.display_order

                FROM ufc_study.event_participant
                     event_participant

                JOIN ufc_study.participant participant
                  ON participant.id =
                     event_participant.participant_id

                WHERE event_participant.event_id = :eventId

                ORDER BY
                    event_participant.display_order,
                    participant.canonical_name
                """,
                new MapSqlParameterSource(
                        "eventId",
                        eventId
                ),
                (resultSet, rowNumber) ->
                        new ResolvableParticipant(
                                resultSet.getObject(
                                        "id",
                                        UUID.class
                                ),
                                resultSet.getString(
                                        "canonical_name"
                                ),
                                resultSet.getInt(
                                        "display_order"
                                )
                        )
        );
    }

    private static String metadataJson(
            String recordedBy
    ) {
        return """
                {
                  "recordedBy": "%s",
                  "recordingMethod": "CONSOLE_MANUAL"
                }
                """.formatted(
                escapeJson(recordedBy)
        );
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank."
            );
        }

        return value.trim();
    }

    private static Instant toInstant(
            OffsetDateTime value
    ) {
        return value == null
                ? null
                : value.toInstant();
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