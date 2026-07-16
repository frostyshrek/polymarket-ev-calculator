package com.ufcstudy.persistence.matching.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;
import java.util.UUID;

public final class ParticipantAliasRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public ParticipantAliasRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void insertIfMissing(
            UUID participantId,
            UUID dataSourceId,
            String aliasName,
            String normalizedAlias
    ) {
        jdbc.update(
                """
                INSERT INTO ufc_study.participant_alias (
                    id,
                    participant_id,
                    data_source_id,
                    alias_name,
                    normalized_alias
                )
                VALUES (
                    :id,
                    :participantId,
                    :dataSourceId,
                    :aliasName,
                    :normalizedAlias
                )
                ON CONFLICT (
                    participant_id,
                    data_source_id,
                    normalized_alias
                )
                DO NOTHING
                """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue(
                                "participantId",
                                participantId
                        )
                        .addValue(
                                "dataSourceId",
                                dataSourceId
                        )
                        .addValue("aliasName", aliasName)
                        .addValue(
                                "normalizedAlias",
                                normalizedAlias
                        )
        );
    }
}