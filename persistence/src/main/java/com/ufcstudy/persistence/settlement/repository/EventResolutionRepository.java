package com.ufcstudy.persistence.settlement.repository;

import com.ufcstudy.persistence.JdbcTime;
import com.ufcstudy.persistence.settlement.model.EventResolutionInsert;
import com.ufcstudy.persistence.settlement.model.FinalEventResolutionRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class EventResolutionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EventResolutionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public boolean sportingEventExists(
            UUID sportingEventId
    ) {
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

    public boolean finalResolutionExists(
            UUID sportingEventId
    ) {
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

    public Optional<FinalEventResolutionRecord>
    findFinalByEventId(
            UUID sportingEventId
    ) {
        List<FinalEventResolutionRecord> resolutions =
                jdbc.query(
                        """
                        SELECT
                            id,
                            sporting_event_id,
                            winning_participant_id,
                            official_result_type,
                            official_result_text,
                            result_source_id,
                            source_external_result_id,
                            official_result_at,
                            observed_at
                        FROM ufc_study.event_resolution
                        WHERE sporting_event_id =
                              :sportingEventId
                          AND is_final = TRUE
                        ORDER BY
                            observed_at DESC,
                            created_at DESC
                        LIMIT 1
                        """,
                        new MapSqlParameterSource()
                                .addValue(
                                        "sportingEventId",
                                        sportingEventId
                                ),
                        (resultSet, rowNumber) ->
                                mapFinalResolution(
                                        resultSet
                                )
                );

        return resolutions.stream().findFirst();
    }

    public void insert(
            EventResolutionInsert resolution
    ) {
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
                        .addValue(
                                "id",
                                resolution.id()
                        )
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

    private static FinalEventResolutionRecord
    mapFinalResolution(
            ResultSet resultSet
    ) throws SQLException {
        return new FinalEventResolutionRecord(
                resultSet.getObject(
                        "id",
                        UUID.class
                ),
                resultSet.getObject(
                        "sporting_event_id",
                        UUID.class
                ),
                resultSet.getObject(
                        "winning_participant_id",
                        UUID.class
                ),
                resultSet.getString(
                        "official_result_type"
                ),
                resultSet.getString(
                        "official_result_text"
                ),
                resultSet.getObject(
                        "result_source_id",
                        UUID.class
                ),
                resultSet.getString(
                        "source_external_result_id"
                ),
                toInstant(
                        resultSet.getObject(
                                "official_result_at",
                                OffsetDateTime.class
                        )
                ),
                toInstant(
                        resultSet.getObject(
                                "observed_at",
                                OffsetDateTime.class
                        )
                )
        );
    }

    private static Instant toInstant(
            OffsetDateTime value
    ) {
        return value == null
                ? null
                : value.toInstant();
    }
}