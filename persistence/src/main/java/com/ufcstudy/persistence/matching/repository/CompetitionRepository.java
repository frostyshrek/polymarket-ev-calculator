package com.ufcstudy.persistence.matching.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class CompetitionRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CompetitionRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID findOrCreateUfc(UUID sportId) {
        UUID existing = jdbc.query(
                """
                SELECT id
                FROM ufc_study.competition
                WHERE sport_id = :sportId
                  AND competition_code = 'UFC'
                """,
                new MapSqlParameterSource()
                        .addValue("sportId", sportId),
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
                INSERT INTO ufc_study.competition (
                    id,
                    sport_id,
                    competition_code,
                    display_name,
                    country_code,
                    is_active
                )
                VALUES (
                    :id,
                    :sportId,
                    'UFC',
                    'Ultimate Fighting Championship',
                    'USA',
                    TRUE
                )
                ON CONFLICT (
                    sport_id,
                    competition_code
                )
                DO NOTHING
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("sportId", sportId)
        );

        return jdbc.queryForObject(
                """
                SELECT id
                FROM ufc_study.competition
                WHERE sport_id = :sportId
                  AND competition_code = 'UFC'
                """,
                new MapSqlParameterSource()
                        .addValue("sportId", sportId),
                UUID.class
        );
    }
}