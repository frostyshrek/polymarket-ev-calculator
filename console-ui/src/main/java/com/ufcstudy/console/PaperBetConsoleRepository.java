package com.ufcstudy.console;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Repository
public class PaperBetConsoleRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PaperBetConsoleRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public List<PaperBetConsoleRow> findOpen(int limit) {
        return findByStatus(
                "OPEN",
                limit
        );
    }

    public List<PaperBetConsoleRow> findSettled(int limit) {
        return findByStatus(
                "SETTLED",
                limit
        );
    }

    private List<PaperBetConsoleRow> findByStatus(
            String status,
            int limit
    ) {
        return jdbc.query(
                """
                SELECT
                    id,
                    bookmaker_code,
                    placed_at,
                    decimal_odds,
                    reference_probability,
                    estimated_ev,
                    stake_units,
                    bet_status,
                    bet_result,
                    net_profit_units
                FROM ufc_study.paper_bet
                WHERE bet_status = :status
                ORDER BY
                    CASE
                        WHEN settled_at IS NOT NULL
                            THEN settled_at
                        ELSE placed_at
                    END DESC,
                    id DESC
                LIMIT :limit
                """,
                new MapSqlParameterSource()
                        .addValue("status", status)
                        .addValue("limit", limit),
                (resultSet, rowNumber) ->
                        new PaperBetConsoleRow(
                                resultSet.getObject(
                                        "id",
                                        UUID.class
                                ),
                                resultSet.getString(
                                        "bookmaker_code"
                                ),
                                resultSet.getObject(
                                        "placed_at",
                                        OffsetDateTime.class
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
                                        "net_profit_units"
                                )
                        )
        );
    }

    public record PaperBetConsoleRow(
            UUID id,
            String bookmakerCode,
            OffsetDateTime placedAt,
            BigDecimal decimalOdds,
            BigDecimal probability,
            BigDecimal estimatedEv,
            BigDecimal stakeUnits,
            String status,
            String result,
            BigDecimal netProfitUnits
    ) {
    }
}