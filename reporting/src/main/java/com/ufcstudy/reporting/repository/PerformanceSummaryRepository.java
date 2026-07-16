package com.ufcstudy.reporting.repository;

import com.ufcstudy.reporting.model.PerformanceSummary;
import com.ufcstudy.reporting.model.ReportFilter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.Objects;

public final class PerformanceSummaryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PerformanceSummaryRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public PerformanceSummary summarize(
            ReportFilter filter
    ) {
        Objects.requireNonNull(filter);

        String sql = """
                SELECT
                    COUNT(*) AS total_bets,

                    COUNT(*) FILTER (
                        WHERE paper_bet.bet_status = 'OPEN'
                    ) AS open_bets,

                    COUNT(*) FILTER (
                        WHERE paper_bet.bet_status = 'SETTLED'
                    ) AS settled_bets,

                    COUNT(*) FILTER (
                        WHERE paper_bet.bet_result = 'WIN'
                    ) AS wins,

                    COUNT(*) FILTER (
                        WHERE paper_bet.bet_result = 'LOSS'
                    ) AS losses,

                    COUNT(*) FILTER (
                        WHERE paper_bet.bet_result = 'VOID'
                    ) AS voids,

                    COALESCE(
                        SUM(paper_bet.stake_units),
                        0
                    ) AS total_stake_units,

                    COALESCE(
                        SUM(paper_bet.stake_units)
                            FILTER (
                                WHERE paper_bet.bet_status =
                                      'SETTLED'
                            ),
                        0
                    ) AS settled_stake_units,

                    COALESCE(
                        SUM(paper_bet.gross_return_units)
                            FILTER (
                                WHERE paper_bet.bet_status =
                                      'SETTLED'
                            ),
                        0
                    ) AS total_gross_return_units,

                    COALESCE(
                        SUM(paper_bet.net_profit_units)
                            FILTER (
                                WHERE paper_bet.bet_status =
                                      'SETTLED'
                            ),
                        0
                    ) AS total_net_profit_units,

                    COALESCE(
                        SUM(paper_bet.net_profit_units)
                            FILTER (
                                WHERE paper_bet.bet_status =
                                      'SETTLED'
                            )
                        /
                        NULLIF(
                            SUM(paper_bet.stake_units)
                                FILTER (
                                    WHERE paper_bet.bet_status =
                                          'SETTLED'
                                ),
                            0
                        ),
                        0
                    ) AS roi,

                    COALESCE(
                        COUNT(*) FILTER (
                            WHERE paper_bet.bet_result =
                                  'WIN'
                        )::NUMERIC
                        /
                        NULLIF(
                            COUNT(*) FILTER (
                                WHERE paper_bet.bet_result
                                      IN ('WIN', 'LOSS')
                            ),
                            0
                        ),
                        0
                    ) AS win_rate,

                    COALESCE(
                        AVG(paper_bet.decimal_odds),
                        0
                    ) AS average_decimal_odds,

                    COALESCE(
                        AVG(
                            paper_bet.reference_probability
                        ),
                        0
                    ) AS average_reference_probability,

                    COALESCE(
                        AVG(paper_bet.estimated_ev),
                        0
                    ) AS average_estimated_ev

                FROM ufc_study.paper_bet paper_bet
                WHERE
                """
                + ReportSqlParameters.predicate();

        return jdbc.queryForObject(
                sql,
                ReportSqlParameters.from(filter),
                (resultSet, rowNumber) ->
                        new PerformanceSummary(
                                resultSet.getLong(
                                        "total_bets"
                                ),
                                resultSet.getLong(
                                        "open_bets"
                                ),
                                resultSet.getLong(
                                        "settled_bets"
                                ),
                                resultSet.getLong(
                                        "wins"
                                ),
                                resultSet.getLong(
                                        "losses"
                                ),
                                resultSet.getLong(
                                        "voids"
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "total_stake_units"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "settled_stake_units"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "total_gross_return_units"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "total_net_profit_units"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "roi"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "win_rate"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "average_decimal_odds"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "average_reference_probability"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "average_estimated_ev"
                                        )
                                )
                        )
        );
    }

    private static BigDecimal decimal(
            BigDecimal value
    ) {
        return value == null
                ? BigDecimal.ZERO
                : value;
    }
}