package com.ufcstudy.persistence.settlement.repository;

import com.ufcstudy.persistence.JdbcTime;
import com.ufcstudy.persistence.settlement.model.EventResolutionInsert;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class EventResolutionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EventResolutionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public boolean sportingEventExists(UUID sportingEventId) {
        Boolean exists = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM ufc_study.sporting_event
                    WHERE id = :sportingEventId
                )
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "sportingEventId",
                                sportingEventId
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }

    public boolean participantBelongsToEvent(
            UUID sportingEventId,
            UUID participantId
    ) {
        Boolean exists = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM ufc_study.event_participant
                    WHERE event_id = :sportingEventId
                      AND participant_id = :participantId
                )
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "sportingEventId",
                                sportingEventId
                        )
                        .addValue(
                                "participantId",
                                participantId
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }

    public boolean finalResolutionExists(UUID sportingEventId) {
        Boolean exists = jdbc.queryForObject(
                """
                SELECT EXISTS (
                    SELECT 1
                    FROM ufc_study.event_resolution
                    WHERE sporting_event_id = :sportingEventId
                      AND is_final = TRUE
                )
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "sportingEventId",
                                sportingEventId
                        ),
                Boolean.class
        );

        return Boolean.TRUE.equals(exists);
    }

    public void insert(EventResolutionInsert resolution) {
        jdbc.update(
                """
                INSERT INTO ufc_study.event_resolution (
                    id,
                    sporting_event_id,
                    winning_participant_id,
                    official_result_type,
                    official_result_text,
                    result_source_id,
                    source_external_result_id,
                    official_result_at,
                    observed_at,
                    is_final,
                    metadata
                )
                VALUES (
                    :id,
                    :sportingEventId,
                    :winningParticipantId,
                    :officialResultType,
                    :officialResultText,
                    :resultSourceId,
                    :sourceExternalResultId,
                    :officialResultAt,
                    :observedAt,
                    :isFinal,
                    CAST(:metadata AS JSONB)
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", resolution.id())
                        .addValue(
                                "sportingEventId",
                                resolution.sportingEventId()
                        )
                        .addValue(
                                "winningParticipantId",
                                resolution.winningParticipantId()
                        )
                        .addValue(
                                "officialResultType",
                                resolution.officialResultType()
                        )
                        .addValue(
                                "officialResultText",
                                resolution.officialResultText()
                        )
                        .addValue(
                                "resultSourceId",
                                resolution.resultSourceId()
                        )
                        .addValue(
                                "sourceExternalResultId",
                                resolution.sourceExternalResultId()
                        )
                        .addValue(
                                "officialResultAt",
                                JdbcTime.from(
                                        resolution.officialResultAt()
                                )
                        )
                        .addValue(
                                "observedAt",
                                JdbcTime.from(
                                        resolution.observedAt()
                                )
                        )
                        .addValue(
                                "isFinal",
                                resolution.finalResult()
                        )
                        .addValue(
                                "metadata",
                                resolution.metadataJson()
                        )
        );
    }
}