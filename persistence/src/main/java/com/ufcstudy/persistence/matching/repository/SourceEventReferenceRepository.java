package com.ufcstudy.persistence.matching.repository;

import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SourceEventReferenceRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SourceEventReferenceRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID insert(
            UUID sportingEventId,
            UUID dataSourceId,
            String externalEventId,
            String externalEventName,
            Instant observedAt
    ) {
        UUID id = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO ufc_study.source_event_reference (
                    id,
                    sporting_event_id,
                    data_source_id,
                    external_event_id,
                    external_event_name,
                    first_seen_at,
                    last_seen_at
                )
                VALUES (
                    :id,
                    :sportingEventId,
                    :dataSourceId,
                    :externalEventId,
                    :externalEventName,
                    :observedAt,
                    :observedAt
                )
                ON CONFLICT (
                    data_source_id,
                    external_event_id
                )
                DO UPDATE SET
                    sporting_event_id =
                        EXCLUDED.sporting_event_id,
                    external_event_name =
                        EXCLUDED.external_event_name,
                    last_seen_at =
                        GREATEST(
                            ufc_study.source_event_reference.last_seen_at,
                            EXCLUDED.last_seen_at
                        )
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue(
                                "sportingEventId",
                                sportingEventId
                        )
                        .addValue(
                                "dataSourceId",
                                dataSourceId
                        )
                        .addValue(
                                "externalEventId",
                                externalEventId
                        )
                        .addValue(
                                "externalEventName",
                                externalEventName
                        )
                        .addValue(
                                "observedAt",
                                JdbcTime.from(observedAt)
                        )
        );

        return id;
    }
}