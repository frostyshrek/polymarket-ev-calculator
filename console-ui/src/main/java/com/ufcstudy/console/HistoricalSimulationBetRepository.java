package com.ufcstudy.console;

import com.ufcstudy.simulation.model.SimulationBet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
public class HistoricalSimulationBetRepository {

    private final JdbcTemplate jdbc;

    public HistoricalSimulationBetRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public List<SimulationBet> findUsableBets() {
        return jdbc.query(
                """
                SELECT
                    decimal_odds,
                    reference_probability,
                    bet_result
                FROM ufc_study.paper_bet
                WHERE bet_status = 'SETTLED'
                  AND bet_result IN ('WIN', 'LOSS')
                ORDER BY settled_at, id
                """,
                (resultSet, rowNumber) ->
                        new SimulationBet(
                                resultSet.getBigDecimal(
                                        "decimal_odds"
                                ).doubleValue(),
                                resultSet.getBigDecimal(
                                        "reference_probability"
                                ).doubleValue(),
                                "WIN".equals(
                                        resultSet.getString(
                                                "bet_result"
                                        )
                                )
                        )
        );
    }
}