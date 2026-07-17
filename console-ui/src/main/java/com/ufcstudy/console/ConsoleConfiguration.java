package com.ufcstudy.console;

import com.ufcstudy.reporting.repository.BookmakerPerformanceRepository;
import com.ufcstudy.reporting.repository.CumulativeProfitRepository;
import com.ufcstudy.reporting.repository.EvBandPerformanceRepository;
import com.ufcstudy.reporting.repository.PaperBetReportRepository;
import com.ufcstudy.reporting.repository.PerformanceSummaryRepository;
import com.ufcstudy.reporting.service.StudyReportingService;
import com.ufcstudy.simulation.engine.BankrollSimulationEngine;
import com.ufcstudy.simulation.engine.BootstrapBetSampler;
import com.ufcstudy.simulation.engine.SimulationSummaryCalculator;
import com.ufcstudy.simulation.service.KellySimulationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class ConsoleConfiguration {

    @Bean
    PerformanceSummaryRepository performanceSummaryRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new PerformanceSummaryRepository(jdbc);
    }

    @Bean
    BookmakerPerformanceRepository bookmakerPerformanceRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new BookmakerPerformanceRepository(jdbc);
    }

    @Bean
    EvBandPerformanceRepository evBandPerformanceRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new EvBandPerformanceRepository(jdbc);
    }

    @Bean
    CumulativeProfitRepository cumulativeProfitRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new CumulativeProfitRepository(jdbc);
    }

    @Bean
    PaperBetReportRepository paperBetReportRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new PaperBetReportRepository(jdbc);
    }

    @Bean
    StudyReportingService studyReportingService(
            PerformanceSummaryRepository summaryRepository,
            BookmakerPerformanceRepository bookmakerRepository,
            EvBandPerformanceRepository evBandRepository,
            CumulativeProfitRepository cumulativeRepository,
            PaperBetReportRepository paperBetRepository
    ) {
        return new StudyReportingService(
                summaryRepository,
                bookmakerRepository,
                evBandRepository,
                cumulativeRepository,
                paperBetRepository
        );
    }

    @Bean
    KellySimulationService kellySimulationService() {
        return new KellySimulationService(
                new BootstrapBetSampler(),
                new BankrollSimulationEngine(),
                new SimulationSummaryCalculator()
        );
    }
}