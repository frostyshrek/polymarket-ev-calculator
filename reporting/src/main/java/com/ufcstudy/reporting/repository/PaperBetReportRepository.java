package com.ufcstudy.reporting.repository;

import com.ufcstudy.reporting.model.PaperBetReportRow;
import com.ufcstudy.reporting.model.ReportFilter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class PaperBetReportRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PaperBetReportRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public List<PaperBetReportRow> findAll(
            ReportFilter filter
    ) {
        Objects.requireNonNull(filter);

        String sql = """
                SELECT
                    paper_bet.id AS paper_bet_id,
                    paper_bet.opportunity_id,
                    paper_bet.strategy_version_id,
                    paper_bet.sporting_event_id,

                    selected_outcome.participant_id
                        AS selected_participant_id,

                    participant.canonical_name
                        AS participant_name,

                    paper_bet.bookmaker_code,
                    paper_bet.stake_method,
                    paper_bet.placed_at,
                    paper_bet.settled_at,
                    paper_bet.decimal_odds,
                    paper_bet.reference_probability,
                    paper_bet.estimated_ev,
                    paper_bet.stake_units,
                    paper_bet.bet_status,
                    paper_bet.bet_result,
                    paper_bet.gross_return_units,
                    paper_bet.net_profit_units

                FROM ufc_study.paper_bet paper_bet

                JOIN ufc_study.source_market_outcome
                     selected_outcome
                  ON selected_outcome.id =
                     paper_bet.sportsbook_outcome_id

                LEFT JOIN ufc_study.participant participant
                  ON participant.id =
                     selected_outcome.participant_id

                WHERE
                """
                + ReportSqlParameters.predicate()
                + """
                ORDER BY paper_bet.placed_at,
                         paper_bet.id
                """;

        return jdbc.query(
                sql,
                ReportSqlParameters.from(filter),
                (resultSet, rowNumber) ->
                        new PaperBetReportRow(
                                resultSet.getObject(
                                        "paper_bet_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "opportunity_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "strategy_version_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "sporting_event_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "selected_participant_id",
                                        UUID.class
                                ),
                                resultSet.getString(
                                        "participant_name"
                                ),
                                resultSet.getString(
                                        "bookmaker_code"
                                ),
                                resultSet.getString(
                                        "stake_method"
                                ),
                                toInstant(
                                        resultSet.getObject(
                                                "placed_at",
                                                OffsetDateTime.class
                                        )
                                ),
                                toInstant(
                                        resultSet.getObject(
                                                "settled_at",
                                                OffsetDateTime.class
                                        )
                                ),
                                resultSet.getBigDecimal(
                                        "decimal_odds"
                                ),
                                resultSet.getBigDecimal(
                                        "reference_probability"
                                ),
                                resultSet.getBigDecimal(
                                        "estimated_ev"
                                ),
                                resultSet.getBigDecimal(
                                        "stake_units"
                                ),
                                resultSet.getString(
                                        "bet_status"
                                ),
                                resultSet.getString(
                                        "bet_result"
                                ),
                                resultSet.getBigDecimal(
                                        "gross_return_units"
                                ),
                                resultSet.getBigDecimal(
                                        "net_profit_units"
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