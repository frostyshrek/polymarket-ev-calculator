package com.ufcstudy.reporting.repository;

import com.ufcstudy.reporting.model.CumulativeProfitPoint;
import com.ufcstudy.reporting.model.ReportFilter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class CumulativeProfitRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public CumulativeProfitRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public List<CumulativeProfitPoint> findAll(
            ReportFilter filter
    ) {
        Objects.requireNonNull(filter);

        String sql = """
                SELECT
                    ROW_NUMBER() OVER (
                        ORDER BY paper_bet.settled_at,
                                 paper_bet.id
                    ) AS sequence_number,

                    paper_bet.id AS paper_bet_id,
                    paper_bet.settled_at,
                    paper_bet.net_profit_units,

                    SUM(paper_bet.net_profit_units)
                        OVER (
                            ORDER BY paper_bet.settled_at,
                                     paper_bet.id
                            ROWS BETWEEN UNBOUNDED PRECEDING
                                     AND CURRENT ROW
                        ) AS cumulative_profit_units

                FROM ufc_study.paper_bet paper_bet

                WHERE paper_bet.bet_status = 'SETTLED'
                  AND
                """
                + ReportSqlParameters.predicate()
                + """
                ORDER BY paper_bet.settled_at,
                         paper_bet.id
                """;

        return jdbc.query(
                sql,
                ReportSqlParameters.from(filter),
                (resultSet, rowNumber) ->
                        new CumulativeProfitPoint(
                                resultSet.getLong(
                                        "sequence_number"
                                ),
                                resultSet.getObject(
                                        "paper_bet_id",
                                        UUID.class
                                ),
                                resultSet.getObject(
                                        "settled_at",
                                        OffsetDateTime.class
                                ).toInstant(),
                                resultSet.getBigDecimal(
                                        "net_profit_units"
                                ),
                                resultSet.getBigDecimal(
                                        "cumulative_profit_units"
                                )
                        )
        );
    }
}