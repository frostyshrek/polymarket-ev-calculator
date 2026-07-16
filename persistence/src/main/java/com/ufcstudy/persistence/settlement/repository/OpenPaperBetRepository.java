package com.ufcstudy.persistence.settlement.repository;

import com.ufcstudy.persistence.settlement.model.OpenPaperBetRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class OpenPaperBetRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OpenPaperBetRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
    }

    public List<OpenPaperBetRecord> findOpenByEventId(
            UUID sportingEventId
    ) {
        return jdbc.query(
                """
                SELECT
                    paper_bet.id AS paper_bet_id,
                    paper_bet.sporting_event_id,
                    selected_outcome.participant_id
                        AS selected_participant_id,
                    paper_bet.stake_units,
                    paper_bet.decimal_odds
                FROM ufc_study.paper_bet paper_bet
                JOIN ufc_study.source_market_outcome selected_outcome
                  ON selected_outcome.id =
                     paper_bet.sportsbook_outcome_id
                WHERE paper_bet.sporting_event_id =
                      :sportingEventId
                  AND paper_bet.bet_status = 'OPEN'
                ORDER BY paper_bet.placed_at,
                         paper_bet.id
                """,
                new MapSqlParameterSource()
                        .addValue(
                                "sportingEventId",
                                sportingEventId
                        ),
                (resultSet, rowNumber) -> {
                    UUID participantId = resultSet.getObject(
                            "selected_participant_id",
                            UUID.class
                    );

                    if (participantId == null) {
                        throw new IllegalStateException(
                                "Paper bet outcome is not linked "
                                        + "to a canonical participant"
                        );
                    }

                    return new OpenPaperBetRecord(
                            resultSet.getObject(
                                    "paper_bet_id",
                                    UUID.class
                            ),
                            resultSet.getObject(
                                    "sporting_event_id",
                                    UUID.class
                            ),
                            participantId,
                            resultSet.getBigDecimal(
                                    "stake_units"
                            ),
                            resultSet.getBigDecimal(
                                    "decimal_odds"
                            )
                    );
                }
        );
    }
}