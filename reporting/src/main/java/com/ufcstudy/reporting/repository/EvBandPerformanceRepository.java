package com.ufcstudy.reporting.repository;

import com.ufcstudy.reporting.model.EvBandPerformance;
import com.ufcstudy.reporting.model.ReportFilter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public final class EvBandPerformanceRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public EvBandPerformanceRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public List<EvBandPerformance> findAll(
            ReportFilter filter
    ) {
        Objects.requireNonNull(filter);

        String sql = """
                WITH classified AS (
                    SELECT
                        paper_bet.*,

                        CASE
                            WHEN estimated_ev < 0.075
                                THEN '0.0500-0.0749'
                            WHEN estimated_ev < 0.100
                                THEN '0.0750-0.0999'
                            WHEN estimated_ev < 0.150
                                THEN '0.1000-0.1499'
                            ELSE '0.1500+'
                        END AS ev_band,

                        CASE
                            WHEN estimated_ev < 0.075
                                THEN 1
                            WHEN estimated_ev < 0.100
                                THEN 2
                            WHEN estimated_ev < 0.150
                                THEN 3
                            ELSE 4
                        END AS ev_band_order

                    FROM ufc_study.paper_bet paper_bet
                    WHERE paper_bet.bet_status = 'SETTLED'
                      AND
                """
                + ReportSqlParameters.predicate()
                + """
                )
                SELECT
                    ev_band,
                    ev_band_order,
                    COUNT(*) AS settled_bets,
                    SUM(stake_units) AS settled_stake_units,
                    SUM(net_profit_units) AS net_profit_units,

                    COALESCE(
                        SUM(net_profit_units)
                        /
                        NULLIF(SUM(stake_units), 0),
                        0
                    ) AS roi

                FROM classified
                GROUP BY ev_band, ev_band_order
                ORDER BY ev_band_order
                """;

        return jdbc.query(
                sql,
                ReportSqlParameters.from(filter),
                (resultSet, rowNumber) -> {
                    int order = resultSet.getInt(
                            "ev_band_order"
                    );

                    BigDecimal minimum = switch (order) {
                        case 1 -> new BigDecimal("0.050");
                        case 2 -> new BigDecimal("0.075");
                        case 3 -> new BigDecimal("0.100");
                        case 4 -> new BigDecimal("0.150");
                        default -> throw new IllegalStateException(
                                "Unknown EV band: " + order
                        );
                    };

                    BigDecimal maximum = switch (order) {
                        case 1 -> new BigDecimal("0.075");
                        case 2 -> new BigDecimal("0.100");
                        case 3 -> new BigDecimal("0.150");
                        case 4 -> null;
                        default -> throw new IllegalStateException(
                                "Unknown EV band: " + order
                        );
                    };

                    return new EvBandPerformance(
                            resultSet.getString("ev_band"),
                            minimum,
                            maximum,
                            resultSet.getLong(
                                    "settled_bets"
                            ),
                            resultSet.getBigDecimal(
                                    "settled_stake_units"
                            ),
                            resultSet.getBigDecimal(
                                    "net_profit_units"
                            ),
                            resultSet.getBigDecimal("roi")
                    );
                }
        );
    }
}