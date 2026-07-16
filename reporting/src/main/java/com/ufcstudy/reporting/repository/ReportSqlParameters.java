package com.ufcstudy.reporting.repository;

import com.ufcstudy.persistence.JdbcTime;
import com.ufcstudy.reporting.model.ReportFilter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

final class ReportSqlParameters {

    private ReportSqlParameters() {
    }

    static MapSqlParameterSource from(ReportFilter filter) {
        return new MapSqlParameterSource()
                .addValue(
                        "strategyVersionId",
                        filter.strategyVersionId()
                )
                .addValue(
                        "bookmakerCode",
                        filter.bookmakerCode()
                )
                .addValue(
                        "stakeMethod",
                        filter.stakeMethod()
                )
                .addValue(
                        "placedFrom",
                        JdbcTime.from(filter.placedFrom())
                )
                .addValue(
                        "placedTo",
                        JdbcTime.from(filter.placedTo())
                );
    }

    static String predicate() {
        return """
                (
                CAST(:strategyVersionId AS UUID) IS NULL
                OR paper_bet.strategy_version_id =
                        CAST(:strategyVersionId AS UUID)
                )
                AND (
                CAST(:bookmakerCode AS VARCHAR) IS NULL
                OR paper_bet.bookmaker_code =
                        CAST(:bookmakerCode AS VARCHAR)
                )
                AND (
                CAST(:stakeMethod AS VARCHAR) IS NULL
                OR paper_bet.stake_method =
                        CAST(:stakeMethod AS VARCHAR)
                )
                AND (
                CAST(:placedFrom AS TIMESTAMPTZ) IS NULL
                OR paper_bet.placed_at >=
                        CAST(:placedFrom AS TIMESTAMPTZ)
                )
                AND (
                CAST(:placedTo AS TIMESTAMPTZ) IS NULL
                OR paper_bet.placed_at <
                        CAST(:placedTo AS TIMESTAMPTZ)
                )
                """;
        }
}