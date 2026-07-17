package com.ufcstudy.eventmatching.automated.persistence;

import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class AutomatedMatchRunRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AutomatedMatchRunRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public UUID start(Instant startedAt) {
        UUID id = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO ufc_study.automated_match_run
                (
                    id,
                    started_at,
                    run_status
                )
                VALUES
                (
                    :id,
                    :startedAt,
                    'RUNNING'
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue(
                                "startedAt",
                                JdbcTime.from(startedAt)
                        )
        );

        return id;
    }

    public void complete(
            UUID runId,
            Instant completedAt,
            int candidatesCreated,
            int matchesApproved,
            int reviewRequired
    ) {
        jdbc.update(
                """
                UPDATE ufc_study.automated_match_run
                SET completed_at = :completedAt,
                    run_status = 'COMPLETED',
                    candidates_created = :candidatesCreated,
                    matches_approved = :matchesApproved,
                    review_required = :reviewRequired
                WHERE id = :runId
                """,
                new MapSqlParameterSource()
                        .addValue("runId", runId)
                        .addValue(
                                "completedAt",
                                JdbcTime.from(completedAt)
                        )
                        .addValue(
                                "candidatesCreated",
                                candidatesCreated
                        )
                        .addValue(
                                "matchesApproved",
                                matchesApproved
                        )
                        .addValue(
                                "reviewRequired",
                                reviewRequired
                        )
        );
    }

    public void fail(
            UUID runId,
            Instant completedAt,
            String errorMessage
    ) {
        jdbc.update(
                """
                UPDATE ufc_study.automated_match_run
                SET completed_at = :completedAt,
                    run_status = 'FAILED',
                    error_message = :errorMessage
                WHERE id = :runId
                """,
                new MapSqlParameterSource()
                        .addValue("runId", runId)
                        .addValue(
                                "completedAt",
                                JdbcTime.from(completedAt)
                        )
                        .addValue(
                                "errorMessage",
                                errorMessage
                        )
        );
    }
}