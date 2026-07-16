package com.ufcstudy.persistence.matching.repository;

import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class SportingEventRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SportingEventRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID insert(
            UUID sportId,
            UUID competitionId,
            String canonicalName,
            Instant scheduledStartTime
    ) {
        UUID id = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO ufc_study.sporting_event (
                    id,
                    sport_id,
                    competition_id,
                    canonical_name,
                    scheduled_start_time,
                    event_status
                )
                VALUES (
                    :id,
                    :sportId,
                    :competitionId,
                    :canonicalName,
                    :scheduledStartTime,
                    'SCHEDULED'
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("sportId", sportId)
                        .addValue(
                                "competitionId",
                                competitionId
                        )
                        .addValue(
                                "canonicalName",
                                canonicalName
                        )
                        .addValue(
                                "scheduledStartTime",
                                JdbcTime.from(scheduledStartTime)
                        )
        );

        return id;
    }
}