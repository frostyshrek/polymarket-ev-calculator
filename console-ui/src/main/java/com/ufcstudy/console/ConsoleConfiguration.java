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

import com.ufcstudy.console.operation.DefaultSportsbookIngestionCommand;
import com.ufcstudy.console.operation.ExistingSportsbookIngestionAdapter;
import com.ufcstudy.console.operation.JdbcAutomatedMatchingCandidateProvider;
import com.ufcstudy.console.operation.SportsbookIngestionCommand;
import java.time.Clock;
import com.ufcstudy.console.operation.SportsbookOperationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.ufcstudy.console.operation.PolymarketOperationProperties;
import com.ufcstudy.console.operation.DefaultPolymarketIngestionCommand;
import com.ufcstudy.console.operation.ExistingPolymarketIngestionAdapter;
import com.ufcstudy.console.operation.PolymarketIngestionCommand;

import com.ufcstudy.console.operation.AutomatedMatchingCandidateProvider;
import com.ufcstudy.console.operation.AutomatedMatchingCommand;
import com.ufcstudy.console.operation.DefaultAutomatedMatchingCommand;

import com.ufcstudy.eventmatching.automated.AutomatedMatchDecisionPolicy;
import com.ufcstudy.eventmatching.automated.AutomatedMatchScorer;
import com.ufcstudy.eventmatching.automated.AutomatedMatchingService;
import com.ufcstudy.eventmatching.automated.FighterNameNormalizer;
import com.ufcstudy.eventmatching.automated.FighterNameSimilarity;
import com.ufcstudy.eventmatching.automated.ParticipantPairScorer;
import com.ufcstudy.eventmatching.automated.ScheduledTimeScorer;

import com.ufcstudy.eventmatching.automated.persistence.AutomatedMatchCandidateRepository;
import com.ufcstudy.eventmatching.automated.persistence.AutomatedMatchRunRepository;
import com.ufcstudy.eventmatching.automated.persistence.ExistingMarketMappingRepository;
import com.ufcstudy.console.operation.JdbcAutomatedMatchingCandidateProvider;

import com.ufcstudy.console.mappingreview.MappingReviewService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.ufcstudy.console.operation.JdbcOpportunityCalculationCommand;
import com.ufcstudy.console.operation.OpportunityCalculationCommand;

import com.ufcstudy.paperbetting.service.PaperBetPlacementService;
import com.ufcstudy.persistence.paperbetting.repository.PaperBetCandidateRepository;
import com.ufcstudy.persistence.paperbetting.repository.PaperBetRepository;

import com.ufcstudy.console.operation.DefaultQualifiedPaperBetPlacementCommand;
import com.ufcstudy.console.operation.QualifiedPaperBetPlacementCommand;
import com.ufcstudy.console.resolution.EventResolutionRecordingService;
import com.ufcstudy.persistence.settlement.repository.EventResolutionRepository;

import com.ufcstudy.console.settlement.CompletedEventSettlementCommand;
import com.ufcstudy.console.settlement.DefaultCompletedEventSettlementCommand;

import com.ufcstudy.persistence.settlement.repository.OpenPaperBetRepository;
import com.ufcstudy.persistence.settlement.repository.PaperBetSettlementRepository;
import com.ufcstudy.persistence.settlement.repository.SettlementAuditRepository;

import com.ufcstudy.settlement.calculation.PaperBetResultResolver;
import com.ufcstudy.settlement.calculation.PaperBetSettlementCalculator;
import com.ufcstudy.console.settlement.service.EventSettlementService;

@Configuration
@EnableConfigurationProperties({
        SportsbookOperationProperties.class,
        PolymarketOperationProperties.class
})
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

    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }

    @Bean
    SportsbookIngestionCommand sportsbookIngestionCommand(
            DefaultSportsbookIngestionCommand
                    .SportsbookIngestionGateway gateway,
            Clock clock
    ) {
        return new DefaultSportsbookIngestionCommand(
                gateway,
                clock
        );
    }

    @Bean
    DefaultSportsbookIngestionCommand.SportsbookIngestionGateway
    sportsbookIngestionGateway(
            ExistingSportsbookIngestionAdapter.SportsbookIngestionRunner runner,
            SportsbookOperationProperties properties
    ) {
        return new ExistingSportsbookIngestionAdapter(
                runner,
                properties
        );
    }

    @Bean
    ExistingSportsbookIngestionAdapter.SportsbookIngestionRunner
    sportsbookIngestionRunner() {
        return () -> {
            throw new IllegalStateException(
                    "The sportsbook ingestion runner has not yet "
                            + "been connected to the existing odds client."
            );
        };
    }

    @Bean
    ExistingPolymarketIngestionAdapter.PolymarketIngestionRunner
    polymarketIngestionRunner() {
        return request -> {
            throw new IllegalStateException(
                    "The Polymarket ingestion runner has not yet "
                            + "been connected to the existing "
                            + "Polymarket client and storage service."
            );
        };
    }

    @Bean
    DefaultPolymarketIngestionCommand.PolymarketIngestionGateway
    polymarketIngestionGateway(
            ExistingPolymarketIngestionAdapter
                    .PolymarketIngestionRunner runner,
            PolymarketOperationProperties properties
    ) {
        return new ExistingPolymarketIngestionAdapter(
                runner,
                properties
        );
    }

    @Bean
    PolymarketIngestionCommand polymarketIngestionCommand(
            DefaultPolymarketIngestionCommand
                    .PolymarketIngestionGateway gateway,
            Clock clock
    ) {
        return new DefaultPolymarketIngestionCommand(
                gateway,
                clock
        );
    }

    @Bean
    FighterNameNormalizer fighterNameNormalizer() {
        return new FighterNameNormalizer();
    }

    @Bean
    FighterNameSimilarity fighterNameSimilarity(
            FighterNameNormalizer normalizer
    ) {
        return new FighterNameSimilarity(normalizer);
    }

    @Bean
    ParticipantPairScorer participantPairScorer(
            FighterNameSimilarity similarity
    ) {
        return new ParticipantPairScorer(similarity);
    }

    @Bean
    ScheduledTimeScorer scheduledTimeScorer() {
        return new ScheduledTimeScorer();
    }

    @Bean
    AutomatedMatchScorer automatedMatchScorer(
            ParticipantPairScorer participantScorer,
            ScheduledTimeScorer timeScorer
    ) {
        return new AutomatedMatchScorer(
                participantScorer,
                timeScorer
        );
    }

    @Bean
        AutomatedMatchDecisionPolicy automatedMatchDecisionPolicy() {
            return new AutomatedMatchDecisionPolicy();
        }

        @Bean
    AutomatedMatchRunRepository automatedMatchRunRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new AutomatedMatchRunRepository(jdbc);
    }

    @Bean
    AutomatedMatchCandidateRepository
    automatedMatchCandidateRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new AutomatedMatchCandidateRepository(jdbc);
    }

    @Bean
    ExistingMarketMappingRepository
    existingMarketMappingRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new ExistingMarketMappingRepository(jdbc);
    }

    @Bean
    AutomatedMatchingService automatedMatchingService(
            AutomatedMatchScorer scorer,
            AutomatedMatchDecisionPolicy decisionPolicy,
            FighterNameNormalizer normalizer,
            AutomatedMatchRunRepository runRepository,
            AutomatedMatchCandidateRepository candidateRepository,
            ExistingMarketMappingRepository mappingRepository,
            Clock clock
    ) {
        return new AutomatedMatchingService(
                scorer,
                decisionPolicy,
                normalizer,
                runRepository,
                candidateRepository,
                mappingRepository,
                clock
        );
    }

    @Bean
    AutomatedMatchingCommand automatedMatchingCommand(
            AutomatedMatchingCandidateProvider candidateProvider,
            AutomatedMatchingService matchingService,
            Clock clock
    ) {
        return new DefaultAutomatedMatchingCommand(
                candidateProvider,
                matchingService,
                clock
        );
    }

    @Bean
    AutomatedMatchingCandidateProvider
    automatedMatchingCandidateProvider(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new JdbcAutomatedMatchingCandidateProvider(
                jdbc
        );
    }

    @Bean
    TransactionTemplate transactionTemplate(
            PlatformTransactionManager transactionManager
    ) {
        return new TransactionTemplate(
                transactionManager
        );
    }

    @Bean
    MappingReviewService mappingReviewService(
            NamedParameterJdbcTemplate jdbc,
            TransactionTemplate transactionTemplate,
            Clock clock
    ) {
        return new MappingReviewService(
                jdbc,
                transactionTemplate,
                clock
        );
    }

    @Bean
    OpportunityCalculationCommand opportunityCalculationCommand(
            NamedParameterJdbcTemplate jdbc,
            TransactionTemplate transactionTemplate,
            Clock clock
    ) {
        return new JdbcOpportunityCalculationCommand(
                jdbc,
                transactionTemplate,
                clock
        );
    }

    @Bean
    PaperBetPlacementService paperBetPlacementService(
            TransactionTemplate transactionTemplate,
            PaperBetCandidateRepository candidateRepository,
            PaperBetRepository paperBetRepository
    ) {
        return new PaperBetPlacementService(
                transactionTemplate,
                candidateRepository,
                paperBetRepository
        );
    }

    @Bean
    QualifiedPaperBetPlacementCommand
    qualifiedPaperBetPlacementCommand(
            NamedParameterJdbcTemplate jdbc,
            PaperBetPlacementService placementService,
            Clock clock
    ) {
        return new DefaultQualifiedPaperBetPlacementCommand(
                jdbc,
                placementService,
                clock
        );
    }

    @Bean
    PaperBetCandidateRepository paperBetCandidateRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new PaperBetCandidateRepository(jdbc);
    }

    @Bean
    PaperBetRepository paperBetRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new PaperBetRepository(jdbc);
    }

    @Bean
    EventResolutionRepository eventResolutionRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new EventResolutionRepository(jdbc);
    }

    @Bean
    EventResolutionRecordingService
    eventResolutionRecordingService(
            NamedParameterJdbcTemplate jdbc,
            EventResolutionRepository resolutionRepository,
            TransactionTemplate transactionTemplate,
            Clock clock
    ) {
        return new EventResolutionRecordingService(
                jdbc,
                resolutionRepository,
                transactionTemplate,
                clock
        );
    }

    @Bean
    OpenPaperBetRepository openPaperBetRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new OpenPaperBetRepository(jdbc);
    }

    @Bean
    PaperBetSettlementRepository
    paperBetSettlementRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new PaperBetSettlementRepository(jdbc);
    }

    @Bean
    SettlementAuditRepository settlementAuditRepository(
            NamedParameterJdbcTemplate jdbc
    ) {
        return new SettlementAuditRepository(jdbc);
    }

    @Bean
    PaperBetResultResolver paperBetResultResolver() {
        return new PaperBetResultResolver();
    }

    @Bean
    PaperBetSettlementCalculator
    paperBetSettlementCalculator() {
        return new PaperBetSettlementCalculator();
    }

    @Bean
    EventSettlementService eventSettlementService(
            TransactionTemplate transactionTemplate,
            EventResolutionRepository resolutionRepository,
            OpenPaperBetRepository openPaperBetRepository,
            PaperBetSettlementRepository settlementRepository,
            SettlementAuditRepository auditRepository,
            PaperBetResultResolver resultResolver,
            PaperBetSettlementCalculator calculator
    ) {
        return new EventSettlementService(
                transactionTemplate,
                resolutionRepository,
                openPaperBetRepository,
                settlementRepository,
                auditRepository,
                resultResolver,
                calculator
        );
    }

    @Bean
    CompletedEventSettlementCommand
    completedEventSettlementCommand(
            NamedParameterJdbcTemplate jdbc,
            EventSettlementService settlementService,
            Clock clock
    ) {
        return new DefaultCompletedEventSettlementCommand(
                jdbc,
                settlementService,
                clock
        );
    }
}