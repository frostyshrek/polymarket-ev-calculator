package com.ufcstudy.eventmatching.automated.persistence;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Objects;

public final class AutomatedMatchCandidateRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AutomatedMatchCandidateRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public void insert(
            AutomatedMatchCandidateInsert candidate
    ) {
        String sql = """
                INSERT INTO ufc_study.automated_match_candidate
                (
                    id,
                    automated_match_run_id,
                    sportsbook_market_id,
                    prediction_market_id,
                    participant_score,
                    scheduled_time_score,
                    overall_score,
                    scheduled_time_difference_seconds,
                    matching_status,
                    decision_reason,
                    sportsbook_participant_key,
                    prediction_participant_key,
                    created_mapping_id
                )
                VALUES
                (
                    :id,
                    :runId,
                    :sportsbookMarketId,
                    :predictionMarketId,
                    :participantScore,
                    :scheduledTimeScore,
                    :overallScore,
                    :scheduledTimeDifferenceSeconds,
                    :matchingStatus,
                    :decisionReason,
                    :sportsbookParticipantKey,
                    :predictionParticipantKey,
                    :createdMappingId
                )
                """;

        jdbc.update(
                sql,
                new MapSqlParameterSource()
                        .addValue("id", candidate.id())
                        .addValue("runId", candidate.runId())
                        .addValue(
                                "sportsbookMarketId",
                                candidate.sportsbookMarketId()
                        )
                        .addValue(
                                "predictionMarketId",
                                candidate.predictionMarketId()
                        )
                        .addValue(
                                "participantScore",
                                candidate.participantScore()
                        )
                        .addValue(
                                "scheduledTimeScore",
                                candidate.scheduledTimeScore()
                        )
                        .addValue(
                                "overallScore",
                                candidate.overallScore()
                        )
                        .addValue(
                                "scheduledTimeDifferenceSeconds",
                                candidate
                                        .scheduledTimeDifferenceSeconds()
                        )
                        .addValue(
                                "matchingStatus",
                                candidate.status().name()
                        )
                        .addValue(
                                "decisionReason",
                                candidate.decisionReason()
                        )
                        .addValue(
                                "sportsbookParticipantKey",
                                candidate.sportsbookParticipantKey()
                        )
                        .addValue(
                                "predictionParticipantKey",
                                candidate.predictionParticipantKey()
                        )
                        .addValue(
                                "createdMappingId",
                                candidate.createdMappingId()
                        )
        );
    }
}