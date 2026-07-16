package com.ufcstudy.persistence.matching.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class EventParticipantRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EventParticipantRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void insert(
            UUID eventId,
            UUID participantId,
            int displayOrder
    ) {
        jdbc.update(
                """
                INSERT INTO ufc_study.event_participant (
                    event_id,
                    participant_id,
                    participant_role,
                    display_order
                )
                VALUES (
                    :eventId,
                    :participantId,
                    'COMPETITOR',
                    :displayOrder
                )
                """,
                new MapSqlParameterSource()
                        .addValue("eventId", eventId)
                        .addValue(
                                "participantId",
                                participantId
                        )
                        .addValue(
                                "displayOrder",
                                displayOrder
                        )
        );
    }
}