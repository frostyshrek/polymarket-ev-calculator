package com.ufcstudy.reporting.repository;

import com.ufcstudy.reporting.model.BookmakerPerformance;
import com.ufcstudy.reporting.model.ReportFilter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class BookmakerPerformanceRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public BookmakerPerformanceRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public List<BookmakerPerformance> findAll(
            ReportFilter filter
    ) {
        Objects.requireNonNull(filter);

        String sql = """
                SELECT
                    paper_bet.bookmaker_code,

                    COUNT(*) AS settled_bets,

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
                    ) AS settled_stake_units,

                    COALESCE(
                        SUM(paper_bet.net_profit_units),
                        0
                    ) AS net_profit_units,

                    COALESCE(
                        SUM(paper_bet.net_profit_units)
                        /
                        NULLIF(
                            SUM(paper_bet.stake_units),
                            0
                        ),
                        0
                    ) AS roi,

                    COALESCE(
                        AVG(paper_bet.estimated_ev),
                        0
                    ) AS average_estimated_ev

                FROM ufc_study.paper_bet paper_bet
                WHERE paper_bet.bet_status = 'SETTLED'
                  AND
                """
                + ReportSqlParameters.predicate()
                + """
                GROUP BY paper_bet.bookmaker_code
                ORDER BY settled_bets DESC,
                         paper_bet.bookmaker_code
                """;

        return jdbc.query(
                sql,
                ReportSqlParameters.from(filter),
                (resultSet, rowNumber) ->
                        new BookmakerPerformance(
                                resultSet.getString(
                                        "bookmaker_code"
                                ),
                                resultSet.getLong(
                                        "settled_bets"
                                ),
                                resultSet.getLong("wins"),
                                resultSet.getLong("losses"),
                                resultSet.getLong("voids"),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "settled_stake_units"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "net_profit_units"
                                        )
                                ),
                                decimal(
                                        resultSet.getBigDecimal(
                                                "roi"
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