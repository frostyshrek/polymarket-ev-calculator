package com.ufcstudy.reporting.service;

import com.ufcstudy.reporting.model.ReportFilter;
import com.ufcstudy.reporting.model.StudyReport;
import com.ufcstudy.reporting.repository.BookmakerPerformanceRepository;
import com.ufcstudy.reporting.repository.CumulativeProfitRepository;
import com.ufcstudy.reporting.repository.EvBandPerformanceRepository;
import com.ufcstudy.reporting.repository.PaperBetReportRepository;
import com.ufcstudy.reporting.repository.PerformanceSummaryRepository;

import java.util.Objects;

public final class StudyReportingService {

    private final PerformanceSummaryRepository summaryRepository;
    private final BookmakerPerformanceRepository bookmakerRepository;
    private final EvBandPerformanceRepository evBandRepository;
    private final CumulativeProfitRepository cumulativeProfitRepository;
    private final PaperBetReportRepository paperBetRepository;

    public StudyReportingService(
            PerformanceSummaryRepository summaryRepository,
            BookmakerPerformanceRepository bookmakerRepository,
            EvBandPerformanceRepository evBandRepository,
            CumulativeProfitRepository cumulativeProfitRepository,
            PaperBetReportRepository paperBetRepository
    ) {
        this.summaryRepository =
                Objects.requireNonNull(summaryRepository);
        this.bookmakerRepository =
                Objects.requireNonNull(bookmakerRepository);
        this.evBandRepository =
                Objects.requireNonNull(evBandRepository);
        this.cumulativeProfitRepository =
                Objects.requireNonNull(
                        cumulativeProfitRepository
                );
        this.paperBetRepository =
                Objects.requireNonNull(paperBetRepository);
    }

    public StudyReport generate(ReportFilter filter) {
        Objects.requireNonNull(filter);

        return new StudyReport(
                filter,
                summaryRepository.summarize(filter),
                bookmakerRepository.findAll(filter),
                evBandRepository.findAll(filter),
                cumulativeProfitRepository.findAll(filter),
                paperBetRepository.findAll(filter)
        );
    }
}