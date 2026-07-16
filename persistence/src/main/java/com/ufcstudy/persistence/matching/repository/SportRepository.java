package com.ufcstudy.persistence.matching.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class SportRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SportRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID findOrCreate(
            String sportCode,
            String displayName
    ) {
        UUID existing = jdbc.query(
                """
                SELECT id
                FROM ufc_study.sport
                WHERE sport_code = :sportCode
                """,
                new MapSqlParameterSource()
                        .addValue("sportCode", sportCode),
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
                INSERT INTO ufc_study.sport (
                    id,
                    sport_code,
                    display_name
                )
                VALUES (
                    :id,
                    :sportCode,
                    :displayName
                )
                ON CONFLICT (sport_code) DO NOTHING
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("sportCode", sportCode)
                        .addValue("displayName", displayName)
        );

        return jdbc.queryForObject(
                """
                SELECT id
                FROM ufc_study.sport
                WHERE sport_code = :sportCode
                """,
                new MapSqlParameterSource()
                        .addValue("sportCode", sportCode),
                UUID.class
        );
    }
}