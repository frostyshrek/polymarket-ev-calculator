package com.ufcstudy.console.mappingreview;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class MappingReviewService {

    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public MappingReviewService(
            NamedParameterJdbcTemplate jdbc,
            TransactionTemplate transactionTemplate,
            Clock clock
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.transactionTemplate =
                Objects.requireNonNull(transactionTemplate);
        this.clock = Objects.requireNonNull(clock);
    }

    public Optional<MappingReviewCandidate>
    findNextReviewCandidate() {

        String sql = """
                SELECT
                    candidate.id,
                    candidate.sportsbook_market_id,
                    sportsbook.market_name
                        AS sportsbook_market_name,
                    candidate.prediction_market_id,
                    prediction.market_name
                        AS prediction_market_name,
                    candidate.participant_score,
                    candidate.scheduled_time_score,
                    candidate.overall_score,
                    candidate.decision_reason
                FROM ufc_study.automated_match_candidate candidate
                JOIN ufc_study.source_market sportsbook
                  ON sportsbook.id =
                     candidate.sportsbook_market_id
                JOIN ufc_study.source_market prediction
                  ON prediction.id =
                     candidate.prediction_market_id
                WHERE candidate.matching_status =
                      'REVIEW_REQUIRED'
                ORDER BY
                    candidate.overall_score DESC,
                    candidate.created_at ASC
                LIMIT 1
                """;

        List<MappingReviewCandidate> candidates =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (resultSet, rowNumber) ->
                                mapCandidate(resultSet)
                );

        return candidates.stream().findFirst();
    }

    public MappingReviewResult approve(
            UUID candidateId,
            UUID firstSportsbookOutcomeId,
            UUID firstPredictionOutcomeId,
            UUID secondSportsbookOutcomeId,
            UUID secondPredictionOutcomeId,
            String reviewer
    ) {
        Objects.requireNonNull(candidateId);
        Objects.requireNonNull(firstSportsbookOutcomeId);
        Objects.requireNonNull(firstPredictionOutcomeId);
        Objects.requireNonNull(secondSportsbookOutcomeId);
        Objects.requireNonNull(secondPredictionOutcomeId);

        String normalizedReviewer =
                requireText(reviewer, "Reviewer");

        try {
            return transactionTemplate.execute(status ->
                    approveInTransaction(
                            candidateId,
                            firstSportsbookOutcomeId,
                            firstPredictionOutcomeId,
                            secondSportsbookOutcomeId,
                            secondPredictionOutcomeId,
                            normalizedReviewer
                    )
            );
        } catch (RuntimeException exception) {
            return MappingReviewResult.failure(
                    errorMessage(exception)
            );
        }
    }

    public MappingReviewResult reject(
            UUID candidateId,
            String reviewer,
            String reason
    ) {
        Objects.requireNonNull(candidateId);

        String normalizedReviewer =
                requireText(reviewer, "Reviewer");

        String normalizedReason =
                requireText(reason, "Rejection reason");

        try {
            return transactionTemplate.execute(status -> {
                lockPendingCandidate(candidateId);

                String decisionReason =
                        truncate(
                                "MANUAL_REJECTED: "
                                        + normalizedReason,
                                128
                        );

                int updated = jdbc.update(
                        """
                        UPDATE ufc_study.automated_match_candidate
                        SET matching_status = 'REJECTED',
                            decision_reason = :decisionReason,
                            reviewed_at = :reviewedAt,
                            reviewed_by = :reviewedBy
                        WHERE id = :candidateId
                          AND matching_status =
                              'REVIEW_REQUIRED'
                        """,
                        new MapSqlParameterSource()
                                .addValue(
                                        "candidateId",
                                        candidateId
                                )
                                .addValue(
                                        "decisionReason",
                                        decisionReason
                                )
                                .addValue(
                                        "reviewedAt",
                                        OffsetDateTime.ofInstant(
                                                clock.instant(),
                                                java.time.ZoneOffset.UTC
                                        )
                                )
                                .addValue(
                                        "reviewedBy",
                                        normalizedReviewer
                                )
                );

                if (updated != 1) {
                    throw new IllegalStateException(
                            "Candidate was not available "
                                    + "for review."
                    );
                }

                return MappingReviewResult.success(
                        "Candidate rejected.",
                        List.of()
                );
            });
        } catch (RuntimeException exception) {
            return MappingReviewResult.failure(
                    errorMessage(exception)
            );
        }
    }

    private MappingReviewResult approveInTransaction(
            UUID candidateId,
            UUID firstSportsbookOutcomeId,
            UUID firstPredictionOutcomeId,
            UUID secondSportsbookOutcomeId,
            UUID secondPredictionOutcomeId,
            String reviewer
    ) {
        LockedCandidate candidate =
                lockPendingCandidate(candidateId);

        validateOutcomePair(
                candidate.sportsbookMarketId(),
                firstSportsbookOutcomeId,
                candidate.predictionMarketId(),
                firstPredictionOutcomeId
        );

        validateOutcomePair(
                candidate.sportsbookMarketId(),
                secondSportsbookOutcomeId,
                candidate.predictionMarketId(),
                secondPredictionOutcomeId
        );

        if (firstSportsbookOutcomeId.equals(
                secondSportsbookOutcomeId
        )) {
            throw new IllegalArgumentException(
                    "The two sportsbook outcomes must differ."
            );
        }

        if (firstPredictionOutcomeId.equals(
                secondPredictionOutcomeId
        )) {
            throw new IllegalArgumentException(
                    "The two Polymarket outcomes must differ."
            );
        }

        ensureNoExistingMapping(
                candidate.sportsbookMarketId(),
                candidate.predictionMarketId()
        );

        UUID firstMappingId = UUID.randomUUID();
        UUID secondMappingId = UUID.randomUUID();

        insertMapping(
                firstMappingId,
                candidate,
                firstSportsbookOutcomeId,
                firstPredictionOutcomeId,
                reviewer
        );

        insertMapping(
                secondMappingId,
                candidate,
                secondSportsbookOutcomeId,
                secondPredictionOutcomeId,
                reviewer
        );

        int updated = jdbc.update(
                """
                UPDATE ufc_study.automated_match_candidate
                SET matching_status = 'AUTO_APPROVED',
                    decision_reason =
                        'MANUAL_REVIEW_APPROVED',
                    created_mapping_id = :mappingId,
                    reviewed_at = :reviewedAt,
                    reviewed_by = :reviewedBy
                WHERE id = :candidateId
                  AND matching_status =
                      'REVIEW_REQUIRED'
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "candidateId",
                                candidateId
                        )
                        .addValue(
                                "mappingId",
                                firstMappingId
                        )
                        .addValue(
                                "reviewedAt",
                                OffsetDateTime.ofInstant(
                                        clock.instant(),
                                        java.time.ZoneOffset.UTC
                                )
                        )
                        .addValue(
                                "reviewedBy",
                                reviewer
                        )
        );

        if (updated != 1) {
            throw new IllegalStateException(
                    "Candidate changed while it was "
                            + "being reviewed."
            );
        }

        return MappingReviewResult.success(
                "Candidate approved and two outcome "
                        + "mappings created.",
                List.of(
                        firstMappingId,
                        secondMappingId
                )
        );
    }

    private LockedCandidate lockPendingCandidate(
            UUID candidateId
    ) {
        String sql = """
                SELECT
                    candidate.id,
                    candidate.sportsbook_market_id,
                    candidate.prediction_market_id,
                    candidate.overall_score,
                    sportsbook_reference.sporting_event_id
                        AS sportsbook_event_id,
                    prediction_reference.sporting_event_id
                        AS prediction_event_id
                FROM ufc_study.automated_match_candidate candidate
                JOIN ufc_study.source_market sportsbook
                  ON sportsbook.id =
                     candidate.sportsbook_market_id
                JOIN ufc_study.source_event_reference
                     sportsbook_reference
                  ON sportsbook_reference.id =
                     sportsbook.source_event_reference_id
                JOIN ufc_study.source_market prediction
                  ON prediction.id =
                     candidate.prediction_market_id
                JOIN ufc_study.source_event_reference
                     prediction_reference
                  ON prediction_reference.id =
                     prediction.source_event_reference_id
                WHERE candidate.id = :candidateId
                  AND candidate.matching_status =
                      'REVIEW_REQUIRED'
                FOR UPDATE OF candidate
                """;

        List<LockedCandidate> candidates =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(
                                "candidateId",
                                candidateId
                        ),
                        (resultSet, rowNumber) ->
                                new LockedCandidate(
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
                                        resultSet.getObject(
                                                "sportsbook_event_id",
                                                UUID.class
                                        ),
                                        resultSet.getObject(
                                                "prediction_event_id",
                                                UUID.class
                                        ),
                                        resultSet.getBigDecimal(
                                                "overall_score"
                                        )
                                )
                );

        LockedCandidate candidate =
                candidates.stream()
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Candidate does not exist "
                                                + "or is no longer "
                                                + "pending review."
                                )
                        );

        if (!candidate.sportsbookEventId().equals(
                candidate.predictionEventId()
        )) {
            throw new IllegalStateException(
                    "The two markets are attached to "
                            + "different sporting events."
            );
        }

        return candidate;
    }

    private void validateOutcomePair(
            UUID sportsbookMarketId,
            UUID sportsbookOutcomeId,
            UUID predictionMarketId,
            UUID predictionOutcomeId
    ) {
        Integer validPairCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ufc_study.source_market_outcome
                             sportsbook_outcome
                        JOIN ufc_study.source_market_outcome
                             prediction_outcome
                          ON prediction_outcome.id =
                             :predictionOutcomeId
                        WHERE sportsbook_outcome.id =
                              :sportsbookOutcomeId
                          AND sportsbook_outcome.source_market_id =
                              :sportsbookMarketId
                          AND prediction_outcome.source_market_id =
                              :predictionMarketId
                          AND sportsbook_outcome.outcome_type =
                              'PARTICIPANT_WIN'
                          AND prediction_outcome.outcome_type =
                              'PARTICIPANT_WIN'
                        """,
                        new MapSqlParameterSource()
                                .addValue(
                                        "sportsbookOutcomeId",
                                        sportsbookOutcomeId
                                )
                                .addValue(
                                        "predictionOutcomeId",
                                        predictionOutcomeId
                                )
                                .addValue(
                                        "sportsbookMarketId",
                                        sportsbookMarketId
                                )
                                .addValue(
                                        "predictionMarketId",
                                        predictionMarketId
                                ),
                        Integer.class
                );

        if (validPairCount == null || validPairCount != 1) {
            throw new IllegalArgumentException(
                    "One or more selected outcomes do not "
                            + "belong to the candidate markets."
            );
        }
    }

    private void ensureNoExistingMapping(
            UUID sportsbookMarketId,
            UUID predictionMarketId
    ) {
        Integer existingCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM ufc_study.market_mapping
                        WHERE sportsbook_market_id =
                              :sportsbookMarketId
                          AND prediction_market_id =
                              :predictionMarketId
                          AND mapping_status IN (
                              'APPROVED_AUTOMATIC',
                              'APPROVED_MANUAL',
                              'PENDING_REVIEW'
                          )
                        """,
                        new MapSqlParameterSource()
                                .addValue(
                                        "sportsbookMarketId",
                                        sportsbookMarketId
                                )
                                .addValue(
                                        "predictionMarketId",
                                        predictionMarketId
                                ),
                        Integer.class
                );

        if (existingCount != null && existingCount > 0) {
            throw new IllegalStateException(
                    "These markets already have an active "
                            + "or pending mapping."
            );
        }
    }

    private void insertMapping(
            UUID mappingId,
            LockedCandidate candidate,
            UUID sportsbookOutcomeId,
            UUID predictionOutcomeId,
            String reviewer
    ) {
        jdbc.update(
                """
                INSERT INTO ufc_study.market_mapping (
                    id,
                    sporting_event_id,
                    sportsbook_market_id,
                    prediction_market_id,
                    sportsbook_outcome_id,
                    prediction_market_outcome_id,
                    mapping_status,
                    settlement_compatibility,
                    match_confidence,
                    manually_approved,
                    approval_notes,
                    approved_by,
                    approved_at
                )
                VALUES (
                    :id,
                    :sportingEventId,
                    :sportsbookMarketId,
                    :predictionMarketId,
                    :sportsbookOutcomeId,
                    :predictionOutcomeId,
                    'APPROVED_MANUAL',
                    'EXACT',
                    :matchConfidence,
                    TRUE,
                    :approvalNotes,
                    :approvedBy,
                    :approvedAt
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", mappingId)
                        .addValue(
                                "sportingEventId",
                                candidate.sportsbookEventId()
                        )
                        .addValue(
                                "sportsbookMarketId",
                                candidate.sportsbookMarketId()
                        )
                        .addValue(
                                "predictionMarketId",
                                candidate.predictionMarketId()
                        )
                        .addValue(
                                "sportsbookOutcomeId",
                                sportsbookOutcomeId
                        )
                        .addValue(
                                "predictionOutcomeId",
                                predictionOutcomeId
                        )
                        .addValue(
                                "matchConfidence",
                                candidate.overallScore()
                        )
                        .addValue(
                                "approvalNotes",
                                "Approved through automated "
                                        + "matching review queue."
                        )
                        .addValue(
                                "approvedBy",
                                reviewer
                        )
                        .addValue(
                                "approvedAt",
                                OffsetDateTime.ofInstant(
                                        clock.instant(),
                                        java.time.ZoneOffset.UTC
                                )
                        )
        );
    }

    private MappingReviewCandidate mapCandidate(
            ResultSet resultSet
    ) throws SQLException {

        UUID candidateId = resultSet.getObject(
                "id",
                UUID.class
        );

        UUID sportsbookMarketId = resultSet.getObject(
                "sportsbook_market_id",
                UUID.class
        );

        UUID predictionMarketId = resultSet.getObject(
                "prediction_market_id",
                UUID.class
        );

        return new MappingReviewCandidate(
                candidateId,
                sportsbookMarketId,
                resultSet.getString(
                        "sportsbook_market_name"
                ),
                predictionMarketId,
                resultSet.getString(
                        "prediction_market_name"
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
                findOutcomes(sportsbookMarketId),
                findOutcomes(predictionMarketId)
        );
    }

    private List<MappingReviewOutcome> findOutcomes(
            UUID marketId
    ) {
        return jdbc.query(
                """
                SELECT
                    id,
                    outcome_name,
                    normalized_outcome_name
                FROM ufc_study.source_market_outcome
                WHERE source_market_id = :marketId
                  AND outcome_type = 'PARTICIPANT_WIN'
                ORDER BY
                    display_order NULLS LAST,
                    outcome_name,
                    id
                """,
                new MapSqlParameterSource(
                        "marketId",
                        marketId
                ),
                (resultSet, rowNumber) ->
                        new MappingReviewOutcome(
                                resultSet.getObject(
                                        "id",
                                        UUID.class
                                ),
                                resultSet.getString(
                                        "outcome_name"
                                ),
                                resultSet.getString(
                                        "normalized_outcome_name"
                                )
                        )
        );
    }

    private static String requireText(
            String value,
            String field
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must not be blank."
            );
        }

        return value.trim();
    }

    private static String truncate(
            String value,
            int maximumLength
    ) {
        return value.length() <= maximumLength
                ? value
                : value.substring(0, maximumLength);
    }

    private static String errorMessage(
            RuntimeException exception
    ) {
        String message = exception.getMessage();

        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }

    private record LockedCandidate(
            UUID candidateId,
            UUID sportsbookMarketId,
            UUID predictionMarketId,
            UUID sportsbookEventId,
            UUID predictionEventId,
            BigDecimal overallScore
    ) {
    }
}