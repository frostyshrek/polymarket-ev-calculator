package com.ufcstudy.persistence.settlement.repository;

import com.ufcstudy.persistence.JdbcTime;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PaperBetSettlementRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public PaperBetSettlementRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public int settle(
            UUID paperBetId,
            String result,
            BigDecimal grossReturnUnits,
            BigDecimal netProfitUnits,
            Instant settledAt
    ) {
        return jdbc.update(
                """
                UPDATE ufc_study.paper_bet
                SET bet_status = 'SETTLED',
                    bet_result = :result,
                    settled_at = :settledAt,
                    gross_return_units = :grossReturnUnits,
                    net_profit_units = :netProfitUnits,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = :paperBetId
                  AND bet_status = 'OPEN'
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "paperBetId",
                                paperBetId
                        )
                        .addValue("result", result)
                        .addValue(
                                "settledAt",
                                JdbcTime.from(settledAt)
                        )
                        .addValue(
                                "grossReturnUnits",
                                grossReturnUnits
                        )
                        .addValue(
                                "netProfitUnits",
                                netProfitUnits
                        )
        );
    }
}