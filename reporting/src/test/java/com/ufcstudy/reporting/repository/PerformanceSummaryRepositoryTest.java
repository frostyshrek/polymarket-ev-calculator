package com.ufcstudy.reporting.repository;

import com.ufcstudy.reporting.ReportingIntegrationSupport;
import com.ufcstudy.reporting.model.ReportFilter;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerformanceSummaryRepositoryTest
        extends ReportingIntegrationSupport {

    @Test
    void returnsZeroSummaryWhenThereAreNoBets() {
        var repository =
                new PerformanceSummaryRepository(
                        jdbc(dataSource())
                );

        var summary = repository.summarize(
                ReportFilter.all()
        );

        assertEquals(0, summary.totalBets());
        assertEquals(0, summary.openBets());
        assertEquals(0, summary.settledBets());
        assertEquals(0, summary.wins());
        assertEquals(0, summary.losses());
        assertEquals(0, summary.voids());

        assertEquals(
                0,
                summary.totalNetProfitUnits()
                        .compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                0,
                summary.roi().compareTo(BigDecimal.ZERO)
        );

        assertEquals(
                0,
                summary.winRate()
                        .compareTo(BigDecimal.ZERO)
        );
    }
}