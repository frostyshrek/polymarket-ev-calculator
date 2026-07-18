package com.ufcstudy.console.operation;

import com.ufcstudy.eventmatching.automated.AutomatedMarketCandidate;
import com.ufcstudy.eventmatching.automated.ParticipantPair;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class JdbcAutomatedMatchingCandidateProvider
        implements AutomatedMatchingCandidateProvider {

    private final NamedParameterJdbcTemplate jdbc;

    public JdbcAutomatedMatchingCandidateProvider(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    @Override
    public List<AutomatedMarketCandidate> findCandidates() {
        String sql = """
                WITH eligible_markets AS (
                    SELECT
                        market.id AS market_id,
                        source.source_type,
                        source_reference.sporting_event_id,
                        event.scheduled_start_time,

                        ARRAY_AGG(
                            outcome.normalized_outcome_name
                            ORDER BY
                                outcome.display_order NULLS LAST,
                                outcome.normalized_outcome_name,
                                outcome.id
                        ) AS participants

                    FROM ufc_study.source_market market

                    JOIN ufc_study.data_source source
                      ON source.id = market.data_source_id

                    JOIN ufc_study.source_event_reference source_reference
                      ON source_reference.id =
                         market.source_event_reference_id

                    JOIN ufc_study.sporting_event event
                      ON event.id =
                         source_reference.sporting_event_id

                    JOIN ufc_study.source_market_outcome outcome
                      ON outcome.source_market_id = market.id
                     AND outcome.outcome_type = 'PARTICIPANT_WIN'

                    WHERE market.market_type = 'MONEYLINE'
                      AND market.market_status IN (
                          'OPEN',
                          'SUSPENDED'
                      )
                      AND market.is_live = FALSE
                      AND source.is_active = TRUE
                      AND source.source_type IN (
                          'SPORTSBOOK_ODDS_PROVIDER',
                          'PREDICTION_MARKET'
                      )

                    GROUP BY
                        market.id,
                        source.source_type,
                        source_reference.sporting_event_id,
                        event.scheduled_start_time

                    HAVING COUNT(*) = 2
                )

                SELECT
                    sportsbook.market_id
                        AS sportsbook_market_id,

                    prediction.market_id
                        AS prediction_market_id,

                    sportsbook.scheduled_start_time
                        AS sportsbook_scheduled_at,

                    prediction.scheduled_start_time
                        AS prediction_scheduled_at,

                    sportsbook.participants[1]
                        AS sportsbook_participant_1,

                    sportsbook.participants[2]
                        AS sportsbook_participant_2,

                    prediction.participants[1]
                        AS prediction_participant_1,

                    prediction.participants[2]
                        AS prediction_participant_2

                FROM eligible_markets sportsbook

                JOIN eligible_markets prediction
                  ON prediction.sporting_event_id =
                     sportsbook.sporting_event_id
                 AND prediction.source_type =
                     'PREDICTION_MARKET'

                WHERE sportsbook.source_type =
                      'SPORTSBOOK_ODDS_PROVIDER'

                  AND sportsbook.market_id <>
                      prediction.market_id

                  AND NOT EXISTS (
                      SELECT 1
                      FROM ufc_study.market_mapping mapping
                      WHERE mapping.sportsbook_market_id =
                            sportsbook.market_id
                         OR mapping.prediction_market_id =
                            prediction.market_id
                  )

                ORDER BY
                    sportsbook.scheduled_start_time,
                    sportsbook.market_id,
                    prediction.market_id
                """;

        return jdbc.query(
                sql,
                new MapSqlParameterSource(),
                (resultSet, rowNumber) ->
                        new AutomatedMarketCandidate(
                                resultSet.getObject(
                                        "sportsbook_market_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "prediction_market_id",
                                        UUID.class
                                ),
                                new ParticipantPair(
                                        List.of(
                                                resultSet.getString(
                                                        "sportsbook_participant_1"
                                                ),
                                                resultSet.getString(
                                                        "sportsbook_participant_2"
                                                )
                                        )
                                ),
                                new ParticipantPair(
                                        List.of(
                                                resultSet.getString(
                                                        "prediction_participant_1"
                                                ),
                                                resultSet.getString(
                                                        "prediction_participant_2"
                                                )
                                        )
                                ),
                                toInstant(
                                        resultSet.getObject(
                                                "sportsbook_scheduled_at",
                                                OffsetDateTime.class
                                        )
                                ),
                                toInstant(
                                        resultSet.getObject(
                                                "prediction_scheduled_at",
                                                OffsetDateTime.class
                                        )
                                )
                        )
        );
    }

    private static java.time.Instant toInstant(
            OffsetDateTime value
    ) {
        return value == null
                ? null
                : value.toInstant();
    }
}