package com.ufcstudy.console.operation;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class JdbcOpportunityCalculationCommand
        implements OpportunityCalculationCommand {

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final int SCALE = 10;

    private final NamedParameterJdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public JdbcOpportunityCalculationCommand(
            NamedParameterJdbcTemplate jdbc,
            TransactionTemplate transactionTemplate,
            Clock clock
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.transactionTemplate =
                Objects.requireNonNull(transactionTemplate);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public OpportunityCalculationResult execute() {
        Instant startedAt = clock.instant();

        try {
            return transactionTemplate.execute(status ->
                    calculate(startedAt)
            );
        } catch (RuntimeException exception) {
            return new OpportunityCalculationResult(
                    null,
                    startedAt,
                    clock.instant(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    false,
                    errorMessage(exception)
            );
        }
    }

    private OpportunityCalculationResult calculate(
            Instant startedAt
    ) {
        StrategyConfiguration strategy =
                loadActiveStrategy();

        int approvedMappings = countApprovedMappings();

        List<CalculationCandidate> candidates =
                loadCandidates();

        int qualified = 0;
        int rejected = 0;
        int duplicatesSkipped = 0;

        Instant decisionTime = clock.instant();

        for (CalculationCandidate candidate : candidates) {
            CalculatedOpportunity opportunity =
                    calculateOpportunity(
                            strategy,
                            candidate,
                            decisionTime
                    );

            try {
                insertOpportunity(
                        strategy,
                        candidate,
                        opportunity,
                        decisionTime
                );

                if ("QUALIFIED".equals(
                        opportunity.qualificationStatus()
                )) {
                    qualified++;
                } else {
                    rejected++;
                }
            } catch (DuplicateKeyException exception) {
                duplicatesSkipped++;
            }
        }

        String message;

        if (approvedMappings == 0) {
            message =
                    "No approved market mappings were found.";
        } else if (candidates.isEmpty()) {
            message =
                    "Approved mappings exist, but no mapping "
                            + "has both sportsbook and "
                            + "Polymarket snapshots.";
        } else {
            message =
                    "Opportunity calculation completed.";
        }

        return new OpportunityCalculationResult(
                strategy.id(),
                startedAt,
                clock.instant(),
                approvedMappings,
                candidates.size(),
                qualified,
                rejected,
                duplicatesSkipped,
                true,
                message
        );
    }

    private StrategyConfiguration loadActiveStrategy() {
        String sql = """
                SELECT
                    id,
                    probability_method,
                    minimum_ev,
                    maximum_market_spread,
                    maximum_snapshot_age_seconds,
                    maximum_source_gap_seconds,
                    minimum_prefight_seconds
                FROM ufc_study.strategy_version
                WHERE strategy_code = 'UFC_EV'
                  AND strategy_status = 'ACTIVE'
                  AND (
                      effective_from IS NULL
                      OR effective_from <= CURRENT_TIMESTAMP
                  )
                  AND (
                      effective_until IS NULL
                      OR effective_until > CURRENT_TIMESTAMP
                  )
                ORDER BY
                    effective_from DESC NULLS LAST,
                    created_at DESC
                LIMIT 1
                """;

        List<StrategyConfiguration> strategies =
                jdbc.query(
                        sql,
                        new MapSqlParameterSource(),
                        (resultSet, rowNumber) ->
                                new StrategyConfiguration(
                                        resultSet.getObject(
                                                "id",
                                                UUID.class
                                        ),
                                        resultSet.getString(
                                                "probability_method"
                                        ),
                                        resultSet.getBigDecimal(
                                                "minimum_ev"
                                        ),
                                        resultSet.getBigDecimal(
                                                "maximum_market_spread"
                                        ),
                                        resultSet.getInt(
                                                "maximum_snapshot_age_seconds"
                                        ),
                                        resultSet.getInt(
                                                "maximum_source_gap_seconds"
                                        ),
                                        resultSet.getInt(
                                                "minimum_prefight_seconds"
                                        )
                                )
                );

        return strategies.stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No active UFC_EV strategy "
                                        + "version was found."
                        )
                );
    }

    private int countApprovedMappings() {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ufc_study.market_mapping
                WHERE mapping_status IN (
                    'APPROVED_AUTOMATIC',
                    'APPROVED_MANUAL'
                )
                  AND settlement_compatibility = 'EXACT'
                """,
                new MapSqlParameterSource(),
                Integer.class
        );

        return count == null ? 0 : count;
    }

    private List<CalculationCandidate> loadCandidates() {
        String sql = """
                SELECT
                    mapping.id AS market_mapping_id,
                    event.scheduled_start_time,

                    sportsbook.id
                        AS sportsbook_snapshot_id,
                    sportsbook.decimal_odds,
                    sportsbook.observed_at
                        AS sportsbook_observed_at,
                    sportsbook.is_live
                        AS sportsbook_is_live,
                    sportsbook.market_suspended,

                    prediction.id
                        AS prediction_snapshot_id,
                    prediction.best_bid,
                    prediction.best_ask,
                    prediction.midpoint,
                    prediction.spread,
                    prediction.observed_at
                        AS prediction_observed_at

                FROM ufc_study.market_mapping mapping

                JOIN ufc_study.sporting_event event
                  ON event.id = mapping.sporting_event_id

                JOIN LATERAL (
                    SELECT snapshot.*
                    FROM ufc_study.sportsbook_odds_snapshot
                         snapshot
                    WHERE snapshot.source_market_id =
                          mapping.sportsbook_market_id
                      AND snapshot.source_outcome_id =
                          mapping.sportsbook_outcome_id
                    ORDER BY snapshot.observed_at DESC
                    LIMIT 1
                ) sportsbook ON TRUE

                JOIN LATERAL (
                    SELECT snapshot.*
                    FROM ufc_study.prediction_market_snapshot
                         snapshot
                    WHERE snapshot.source_market_id =
                          mapping.prediction_market_id
                      AND snapshot.source_outcome_id =
                          mapping.prediction_market_outcome_id
                    ORDER BY snapshot.observed_at DESC
                    LIMIT 1
                ) prediction ON TRUE

                WHERE mapping.mapping_status IN (
                    'APPROVED_AUTOMATIC',
                    'APPROVED_MANUAL'
                )
                  AND mapping.settlement_compatibility =
                      'EXACT'

                ORDER BY
                    event.scheduled_start_time,
                    mapping.id
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (resultSet, rowNumber) ->
                        mapCandidate(resultSet)
        );
    }

    private CalculationCandidate mapCandidate(
            ResultSet resultSet
    ) throws SQLException {
        return new CalculationCandidate(
                resultSet.getObject(
                        "market_mapping_id",
                        UUID.class
                ),
                toInstant(
                        resultSet.getObject(
                                "scheduled_start_time",
                                OffsetDateTime.class
                        )
                ),
                resultSet.getObject(
                        "sportsbook_snapshot_id",
                        UUID.class
                ),
                resultSet.getBigDecimal(
                        "decimal_odds"
                ),
                toInstant(
                        resultSet.getObject(
                                "sportsbook_observed_at",
                                OffsetDateTime.class
                        )
                ),
                resultSet.getBoolean(
                        "sportsbook_is_live"
                ),
                resultSet.getBoolean(
                        "market_suspended"
                ),
                resultSet.getObject(
                        "prediction_snapshot_id",
                        UUID.class
                ),
                resultSet.getBigDecimal(
                        "best_bid"
                ),
                resultSet.getBigDecimal(
                        "best_ask"
                ),
                resultSet.getBigDecimal(
                        "midpoint"
                ),
                resultSet.getBigDecimal(
                        "spread"
                ),
                toInstant(
                        resultSet.getObject(
                                "prediction_observed_at",
                                OffsetDateTime.class
                        )
                )
        );
    }

    private CalculatedOpportunity calculateOpportunity(
            StrategyConfiguration strategy,
            CalculationCandidate candidate,
            Instant decisionTime
    ) {
        BigDecimal referenceProbability =
                selectProbability(
                        strategy.probabilityMethod(),
                        candidate
                );

        BigDecimal decimalOdds =
                candidate.decimalOdds();

        BigDecimal impliedProbability =
                ONE.divide(
                        decimalOdds,
                        SCALE,
                        RoundingMode.HALF_UP
                );

        BigDecimal estimatedEv =
                referenceProbability
                        .multiply(decimalOdds)
                        .subtract(ONE)
                        .setScale(
                                SCALE,
                                RoundingMode.HALF_UP
                        );

        int sportsbookAge =
                nonNegativeSecondsBetween(
                        candidate.sportsbookObservedAt(),
                        decisionTime
                );

        int predictionAge =
                nonNegativeSecondsBetween(
                        candidate.predictionObservedAt(),
                        decisionTime
                );

        int sourceGap =
                absoluteSecondsBetween(
                        candidate.sportsbookObservedAt(),
                        candidate.predictionObservedAt()
                );

        long secondsUntilStartLong =
                Duration.between(
                        decisionTime,
                        candidate.scheduledStartTime()
                ).getSeconds();

        int secondsUntilStart =
                safeInteger(secondsUntilStartLong);

        Qualification qualification =
                qualify(
                        strategy,
                        candidate,
                        referenceProbability,
                        estimatedEv,
                        sportsbookAge,
                        predictionAge,
                        sourceGap,
                        secondsUntilStart
                );

        return new CalculatedOpportunity(
                referenceProbability,
                impliedProbability,
                estimatedEv,
                candidate.spread(),
                sportsbookAge,
                predictionAge,
                sourceGap,
                secondsUntilStart,
                qualification.status(),
                qualification.reason(),
                qualification.rejectionCode()
        );
    }

    private BigDecimal selectProbability(
            String probabilityMethod,
            CalculationCandidate candidate
    ) {
        return switch (probabilityMethod) {
            case "BEST_BID" -> candidate.bestBid();
            case "MIDPOINT" -> candidate.midpoint();
            case "LAST_TRADE" ->
                    throw new IllegalStateException(
                            "LAST_TRADE is not available in "
                                    + "this calculation candidate."
                    );
            default ->
                    throw new IllegalStateException(
                            "Unsupported probability method: "
                                    + probabilityMethod
                    );
        };
    }

    private Qualification qualify(
            StrategyConfiguration strategy,
            CalculationCandidate candidate,
            BigDecimal referenceProbability,
            BigDecimal estimatedEv,
            int sportsbookAge,
            int predictionAge,
            int sourceGap,
            int secondsUntilStart
    ) {
        if (referenceProbability == null
                || referenceProbability.signum() <= 0
                || referenceProbability.compareTo(ONE) >= 0) {
            return rejected(
                    "INVALID_REFERENCE_PROBABILITY",
                    "Reference probability must be "
                            + "strictly between zero and one."
            );
        }

        if (candidate.sportsbookIsLive()) {
            return rejected(
                    "LIVE_MARKET",
                    "Live sportsbook markets are excluded."
            );
        }

        if (candidate.marketSuspended()) {
            return rejected(
                    "SPORTSBOOK_MARKET_SUSPENDED",
                    "The sportsbook market is suspended."
            );
        }

        if (sportsbookAge >
                strategy.maximumSnapshotAgeSeconds()) {
            return rejected(
                    "SPORTSBOOK_SNAPSHOT_STALE",
                    "The sportsbook snapshot exceeds the "
                            + "maximum permitted age."
            );
        }

        if (predictionAge >
                strategy.maximumSnapshotAgeSeconds()) {
            return rejected(
                    "PREDICTION_SNAPSHOT_STALE",
                    "The Polymarket snapshot exceeds the "
                            + "maximum permitted age."
            );
        }

        if (sourceGap >
                strategy.maximumSourceGapSeconds()) {
            return rejected(
                    "SOURCE_GAP_TOO_LARGE",
                    "The two snapshots are not sufficiently "
                            + "synchronized."
            );
        }

        if (secondsUntilStart <
                strategy.minimumPrefightSeconds()) {
            return rejected(
                    "INSUFFICIENT_PREFIGHT_TIME",
                    "The event is too close to its scheduled "
                            + "start time."
            );
        }

        if (candidate.spread().compareTo(
                strategy.maximumMarketSpread()
        ) > 0) {
            return rejected(
                    "PREDICTION_SPREAD_TOO_WIDE",
                    "The Polymarket spread exceeds the "
                            + "strategy maximum."
            );
        }

        if (estimatedEv.compareTo(
                strategy.minimumEv()
        ) < 0) {
            return rejected(
                    "EV_BELOW_THRESHOLD",
                    "Estimated EV is below the strategy "
                            + "minimum."
            );
        }

        return new Qualification(
                "QUALIFIED",
                "All strategy qualification rules passed.",
                null
        );
    }

    private static Qualification rejected(
            String rejectionCode,
            String reason
    ) {
        return new Qualification(
                "REJECTED",
                reason,
                rejectionCode
        );
    }

    private void insertOpportunity(
            StrategyConfiguration strategy,
            CalculationCandidate candidate,
            CalculatedOpportunity opportunity,
            Instant decisionTime
    ) {
        jdbc.update(
                """
                INSERT INTO ufc_study.opportunity (
                    id,
                    strategy_version_id,
                    market_mapping_id,
                    sportsbook_snapshot_id,
                    prediction_snapshot_id,
                    decision_time,
                    reference_probability,
                    probability_method,
                    sportsbook_decimal_odds,
                    raw_implied_probability,
                    sportsbook_no_vig_probability,
                    estimated_ev,
                    expected_profit_per_unit,
                    prediction_market_spread,
                    sportsbook_snapshot_age_seconds,
                    prediction_snapshot_age_seconds,
                    source_gap_seconds,
                    seconds_until_scheduled_start,
                    qualification_status,
                    qualification_reason,
                    rejection_code,
                    calculation_version,
                    opposing_sportsbook_snapshot_id
                )
                VALUES (
                    :id,
                    :strategyVersionId,
                    :marketMappingId,
                    :sportsbookSnapshotId,
                    :predictionSnapshotId,
                    :decisionTime,
                    :referenceProbability,
                    :probabilityMethod,
                    :sportsbookDecimalOdds,
                    :rawImpliedProbability,
                    NULL,
                    :estimatedEv,
                    :expectedProfitPerUnit,
                    :predictionMarketSpread,
                    :sportsbookSnapshotAgeSeconds,
                    :predictionSnapshotAgeSeconds,
                    :sourceGapSeconds,
                    :secondsUntilScheduledStart,
                    :qualificationStatus,
                    :qualificationReason,
                    :rejectionCode,
                    :calculationVersion,
                    NULL
                )
                """,
                new MapSqlParameterSource()
                        .addValue("id", UUID.randomUUID())
                        .addValue(
                                "strategyVersionId",
                                strategy.id()
                        )
                        .addValue(
                                "marketMappingId",
                                candidate.marketMappingId()
                        )
                        .addValue(
                                "sportsbookSnapshotId",
                                candidate.sportsbookSnapshotId()
                        )
                        .addValue(
                                "predictionSnapshotId",
                                candidate.predictionSnapshotId()
                        )
                        .addValue(
                                "decisionTime",
                                OffsetDateTime.ofInstant(
                                        decisionTime,
                                        ZoneOffset.UTC
                                )
                        )
                        .addValue(
                                "referenceProbability",
                                opportunity.referenceProbability()
                        )
                        .addValue(
                                "probabilityMethod",
                                strategy.probabilityMethod()
                        )
                        .addValue(
                                "sportsbookDecimalOdds",
                                candidate.decimalOdds()
                        )
                        .addValue(
                                "rawImpliedProbability",
                                opportunity.rawImpliedProbability()
                        )
                        .addValue(
                                "estimatedEv",
                                opportunity.estimatedEv()
                        )
                        .addValue(
                                "expectedProfitPerUnit",
                                opportunity.estimatedEv()
                        )
                        .addValue(
                                "predictionMarketSpread",
                                opportunity.spread()
                        )
                        .addValue(
                                "sportsbookSnapshotAgeSeconds",
                                opportunity.sportsbookAgeSeconds()
                        )
                        .addValue(
                                "predictionSnapshotAgeSeconds",
                                opportunity.predictionAgeSeconds()
                        )
                        .addValue(
                                "sourceGapSeconds",
                                opportunity.sourceGapSeconds()
                        )
                        .addValue(
                                "secondsUntilScheduledStart",
                                opportunity.secondsUntilStart()
                        )
                        .addValue(
                                "qualificationStatus",
                                opportunity.qualificationStatus()
                        )
                        .addValue(
                                "qualificationReason",
                                opportunity.qualificationReason()
                        )
                        .addValue(
                                "rejectionCode",
                                opportunity.rejectionCode()
                        )
                        .addValue(
                                "calculationVersion",
                                "UFC_EV_1.0"
                        )
        );
    }

    private static int nonNegativeSecondsBetween(
            Instant earlier,
            Instant later
    ) {
        long seconds = Duration.between(
                earlier,
                later
        ).getSeconds();

        return safeInteger(Math.max(0, seconds));
    }

    private static int absoluteSecondsBetween(
            Instant first,
            Instant second
    ) {
        long seconds = Math.abs(
                Duration.between(
                        first,
                        second
                ).getSeconds()
        );

        return safeInteger(seconds);
    }

    private static int safeInteger(long value) {
        if (value > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (value < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        return (int) value;
    }

    private static Instant toInstant(
            OffsetDateTime value
    ) {
        return value == null ? null : value.toInstant();
    }

    private static String errorMessage(
            RuntimeException exception
    ) {
        String message = exception.getMessage();

        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }

    private record StrategyConfiguration(
            UUID id,
            String probabilityMethod,
            BigDecimal minimumEv,
            BigDecimal maximumMarketSpread,
            int maximumSnapshotAgeSeconds,
            int maximumSourceGapSeconds,
            int minimumPrefightSeconds
    ) {
    }

    private record CalculationCandidate(
            UUID marketMappingId,
            Instant scheduledStartTime,
            UUID sportsbookSnapshotId,
            BigDecimal decimalOdds,
            Instant sportsbookObservedAt,
            boolean sportsbookIsLive,
            boolean marketSuspended,
            UUID predictionSnapshotId,
            BigDecimal bestBid,
            BigDecimal bestAsk,
            BigDecimal midpoint,
            BigDecimal spread,
            Instant predictionObservedAt
    ) {
    }

    private record CalculatedOpportunity(
            BigDecimal referenceProbability,
            BigDecimal rawImpliedProbability,
            BigDecimal estimatedEv,
            BigDecimal spread,
            int sportsbookAgeSeconds,
            int predictionAgeSeconds,
            int sourceGapSeconds,
            int secondsUntilStart,
            String qualificationStatus,
            String qualificationReason,
            String rejectionCode
    ) {
    }

    private record Qualification(
            String status,
            String reason,
            String rejectionCode
    ) {
    }
}