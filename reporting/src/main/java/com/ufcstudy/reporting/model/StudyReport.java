package com.ufcstudy.reporting.model;

import java.util.List;
import java.util.Objects;

public record StudyReport(
        ReportFilter filter,
        PerformanceSummary summary,
        List<BookmakerPerformance> bookmakers,
        List<EvBandPerformance> evBands,
        List<CumulativeProfitPoint> cumulativeProfit,
        List<PaperBetReportRow> paperBets
) {

    public StudyReport {
        Objects.requireNonNull(filter);
        Objects.requireNonNull(summary);
        Objects.requireNonNull(bookmakers);
        Objects.requireNonNull(evBands);
        Objects.requireNonNull(cumulativeProfit);
        Objects.requireNonNull(paperBets);

        bookmakers = List.copyOf(bookmakers);
        evBands = List.copyOf(evBands);
        cumulativeProfit = List.copyOf(cumulativeProfit);
        paperBets = List.copyOf(paperBets);
    }
}