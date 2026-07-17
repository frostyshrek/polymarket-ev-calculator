package com.ufcstudy.console;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class MatchingReviewRepository {

    private final JdbcTemplate jdbc;

    public MatchingReviewRepository(JdbcTemplate jdbc) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public List<MatchingReviewRow> findReviewQueue(
            int limit
    ) {
        return jdbc.query(
                """
                SELECT
                    id,
                    sportsbook_market_id,
                    prediction_market_id,
                    participant_score,
                    scheduled_time_score,
                    overall_score,
                    decision_reason,
                    sportsbook_participant_key,
                    prediction_participant_key,
                    created_at
                FROM ufc_study.automated_match_candidate
                WHERE matching_status = 'REVIEW_REQUIRED'
                ORDER BY overall_score DESC,
                         created_at,
                         id
                LIMIT ?
                """,
                (resultSet, rowNumber) ->
                        new MatchingReviewRow(
                                resultSet.getObject(
                                        "id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "sportsbook_market_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "prediction_market_id",
                                        UUID.class
                                ),
                                resultSet.getBigDecimal(
                                        "participant_score"
                                ),
                                resultSet.getBigDecimal(
                                        "scheduled_time_score"
                                ),
                                resultSet.getBigDecimal(
                                        "overall_score"
                                ),
                                resultSet.getString(
                                        "decision_reason"
                                ),
                                resultSet.getString(
                                        "sportsbook_participant_key"
                                ),
                                resultSet.getString(
                                        "prediction_participant_key"
                                ),
                                resultSet.getObject(
                                        "created_at",
                                        OffsetDateTime.class
                                )
                        ),
                limit
        );
    }

    public record MatchingReviewRow(
            UUID id,
            UUID sportsbookMarketId,
            UUID predictionMarketId,
            BigDecimal participantScore,
            BigDecimal scheduledTimeScore,
            BigDecimal overallScore,
            String reason,
            String sportsbookParticipants,
            String predictionParticipants,
            OffsetDateTime createdAt
    ) {
    }
}