package com.ufcstudy.persistence.matching.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class ParticipantRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ParticipantRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID findOrCreate(
            UUID sportId,
            String canonicalName,
            String normalizedName
    ) {
        UUID existing = jdbc.query(
                """
                SELECT id
                FROM ufc_study.participant
                WHERE sport_id = :sportId
                  AND normalized_name = :normalizedName
                """,
                new MapSqlParameterSource()
                        .addValue("sportId", sportId)
                        .addValue(
                                "normalizedName",
                                normalizedName
                        ),
                resultSet -> resultSet.next()
                        ? resultSet.getObject("id", UUID.class)
                        : null
        );

        if (existing != null) {
            return existing;
        }

        UUID id = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO ufc_study.participant (
                    id,
                    sport_id,
                    canonical_name,
                    normalized_name
                )
                VALUES (
                    :id,
                    :sportId,
                    :canonicalName,
                    :normalizedName
                )
                ON CONFLICT (
                    sport_id,
                    normalized_name
                )
                DO NOTHING
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("sportId", sportId)
                        .addValue(
                                "canonicalName",
                                canonicalName
                        )
                        .addValue(
                                "normalizedName",
                                normalizedName
                        )
        );

        return jdbc.queryForObject(
                """
                SELECT id
                FROM ufc_study.participant
                WHERE sport_id = :sportId
                  AND normalized_name = :normalizedName
                """,
                new MapSqlParameterSource()
                        .addValue("sportId", sportId)
                        .addValue(
                                "normalizedName",
                                normalizedName
                        ),
                UUID.class
        );
    }
}