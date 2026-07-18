package com.ufcstudy.console;

import com.ufcstudy.reporting.model.ReportFilter;
import com.ufcstudy.reporting.service.StudyReportingService;
import com.ufcstudy.simulation.model.SimulationConfiguration;
import com.ufcstudy.simulation.service.KellySimulationService;
import com.ufcstudy.simulation.staking.CappedFractionalKellyStrategy;
import com.ufcstudy.simulation.staking.FlatStakeStrategy;
import com.ufcstudy.simulation.staking.KellyFractionCalculator;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import com.ufcstudy.console.operation.SportsbookIngestionCommand;
import com.ufcstudy.console.operation.PolymarketIngestionCommand;
import com.ufcstudy.console.operation.PolymarketIngestionResult;

import com.ufcstudy.console.operation.AutomatedMatchingCommand;
import com.ufcstudy.console.operation.AutomatedMatchingConsoleResult;

import com.ufcstudy.console.mappingreview.MappingReviewCandidate;
import com.ufcstudy.console.mappingreview.MappingReviewOutcome;
import com.ufcstudy.console.mappingreview.MappingReviewResult;
import com.ufcstudy.console.mappingreview.MappingReviewService;

import com.ufcstudy.console.operation.OpportunityCalculationCommand;
import com.ufcstudy.console.operation.OpportunityCalculationResult;
import com.ufcstudy.console.operation.QualifiedPaperBetPlacementCommand;
import com.ufcstudy.console.operation.QualifiedPaperBetPlacementResult;

import com.ufcstudy.console.resolution.EventResolutionRecordingService;
import com.ufcstudy.console.resolution.RecordResolutionResult;
import com.ufcstudy.console.resolution.ResolvableEvent;
import com.ufcstudy.console.resolution.ResolvableParticipant;

import com.ufcstudy.console.settlement.CompletedEventSettlementCommand;
import com.ufcstudy.console.settlement.CompletedEventSettlementResult;
import com.ufcstudy.console.settlement.SettleableEvent;

@Component
public class ConsoleDashboard
        implements CommandLineRunner {

    private final StudyReportingService reportingService;
    private final PaperBetConsoleRepository paperBetRepository;
    private final MatchingReviewRepository matchingRepository;
    private final DatabaseStatusRepository databaseRepository;
    private final HistoricalSimulationBetRepository simulationBetRepository;
    private final KellySimulationService simulationService;
    private final ConfigurableApplicationContext context;
    private final SportsbookIngestionCommand sportsbookIngestionCommand;
    private final PolymarketIngestionCommand polymarketIngestionCommand;
    private final AutomatedMatchingCommand automatedMatchingCommand;
    private final MappingReviewService mappingReviewService;
    private final OpportunityCalculationCommand opportunityCalculationCommand;
    private final QualifiedPaperBetPlacementCommand qualifiedPaperBetPlacementCommand;
    private final EventResolutionRecordingService eventResolutionRecordingService;
    private final CompletedEventSettlementCommand completedEventSettlementCommand;

    private final ConsoleInput input = new ConsoleInput();

    public ConsoleDashboard(
                StudyReportingService reportingService,
                PaperBetConsoleRepository paperBetRepository,
                MatchingReviewRepository matchingRepository,
                DatabaseStatusRepository databaseRepository,
                HistoricalSimulationBetRepository simulationBetRepository,
                KellySimulationService simulationService,
                SportsbookIngestionCommand sportsbookIngestionCommand,
                PolymarketIngestionCommand polymarketIngestionCommand,
                AutomatedMatchingCommand automatedMatchingCommand,
                MappingReviewService mappingReviewService,
                ConfigurableApplicationContext context,
                OpportunityCalculationCommand opportunityCalculationCommand,
                QualifiedPaperBetPlacementCommand qualifiedPaperBetPlacementCommand,
                EventResolutionRecordingService eventResolutionRecordingService,
                CompletedEventSettlementCommand completedEventSettlementCommand
        ) {
        this.reportingService =
                Objects.requireNonNull(reportingService);
        this.paperBetRepository =
                Objects.requireNonNull(paperBetRepository);
        this.matchingRepository =
                Objects.requireNonNull(matchingRepository);
        this.databaseRepository =
                Objects.requireNonNull(databaseRepository);
        this.simulationBetRepository =
                Objects.requireNonNull(simulationBetRepository);
        this.simulationService =
                Objects.requireNonNull(simulationService);
        this.sportsbookIngestionCommand =
                Objects.requireNonNull(
                        sportsbookIngestionCommand
                );
        this.polymarketIngestionCommand =
                Objects.requireNonNull(
                        polymarketIngestionCommand
                );
        this.automatedMatchingCommand =
                Objects.requireNonNull(
                        automatedMatchingCommand
                );
        this.context = Objects.requireNonNull(context);
        this.mappingReviewService =
                Objects.requireNonNull(
                        mappingReviewService
                );
        this.opportunityCalculationCommand =
                Objects.requireNonNull(
                        opportunityCalculationCommand
                );
        this.qualifiedPaperBetPlacementCommand =
                Objects.requireNonNull(
                        qualifiedPaperBetPlacementCommand
                );
        this.eventResolutionRecordingService =
                Objects.requireNonNull(
                        eventResolutionRecordingService
                );
        this.completedEventSettlementCommand =
                Objects.requireNonNull(
                        completedEventSettlementCommand
                );
        }

    @Override
    public void run(String... args) {
        boolean running = true;

        while (running) {
            printMenu();

            String selection =
                    input.readLine("Select an option: ");

            try {
                switch (selection) {
                        case "1" -> runSportsbookIngestion();

                        case "2" -> runPolymarketIngestion();
                        case "3" -> runAutomatedMatching();
                        case "4" -> reviewMappings();
                        case "5" -> calculateOpportunities();
                        case "6" -> placeQualifiedPaperBets();
                        case "7" -> recordEventResolution();
                        case "8" -> settleCompletedEvents();

                        case "11" -> showSummary();
                        case "12" -> showBookmakers();
                        case "13" -> showEvBands();
                        case "14" -> showOpenBets();
                        case "15" -> showRecentSettledBets();
                        case "16" -> showCumulativeProfit();
                        case "17" -> showMatchingReviewQueue();
                        case "18" -> runKellySimulation();
                        case "19" -> showDatabaseStatus();

                        case "0" -> running = false;

                        default -> System.out.println(
                                "Unknown option."
                        );
                        }
            } catch (RuntimeException exception) {
                System.out.println();
                System.out.println(
                        "Operation failed: "
                                + exception.getMessage()
                );
            }

            if (running) {
                input.pause();
            }
        }

        System.out.println("Closing UFC Study Console.");

        context.close();
    }

    private void printMenu() {
        System.out.println();
        System.out.println(
                "========================================"
        );
        System.out.println(
                "     UFC MARKET VALUE STUDY CONSOLE"
        );
        System.out.println(
                "========================================"
        );

        System.out.println("OPERATIONS");
        System.out.println("1. Run sportsbook ingestion");
        System.out.println("2. Run Polymarket ingestion");
        System.out.println("3. Run automated matching");
        System.out.println("4. Review mappings");
        System.out.println("5. Calculate opportunities");
        System.out.println("6. Place qualified paper bets");
        System.out.println("7. Record event resolution");
        System.out.println("8. Settle completed events");

        System.out.println();
        System.out.println("ANALYTICS");
        System.out.println("11. Study summary");
        System.out.println("12. Performance by bookmaker");
        System.out.println("13. Performance by EV band");
        System.out.println("14. Open paper bets");
        System.out.println("15. Recent settled bets");
        System.out.println("16. Cumulative profit");
        System.out.println("17. Automated-match review queue");
        System.out.println("18. Run Kelly simulation");
        System.out.println("19. Database status");

        System.out.println();
        System.out.println("0. Exit");
        System.out.println();
        }

    private void showSummary() {
        var report = reportingService.generate(
                ReportFilter.all()
        );

        var summary = report.summary();

        new ConsoleTable()
                .headers("Metric", "Value")
                .row("Total bets", summary.totalBets())
                .row("Open bets", summary.openBets())
                .row("Settled bets", summary.settledBets())
                .row("Wins", summary.wins())
                .row("Losses", summary.losses())
                .row("Voids", summary.voids())
                .row(
                        "Total stake",
                        number(summary.totalStakeUnits())
                )
                .row(
                        "Settled stake",
                        number(summary.settledStakeUnits())
                )
                .row(
                        "Net profit",
                        number(summary.totalNetProfitUnits())
                )
                .row(
                        "ROI",
                        percent(summary.roi())
                )
                .row(
                        "Win rate",
                        percent(summary.winRate())
                )
                .row(
                        "Average odds",
                        number(summary.averageDecimalOdds())
                )
                .row(
                        "Average probability",
                        percent(
                                summary
                                        .averageReferenceProbability()
                        )
                )
                .row(
                        "Average estimated EV",
                        percent(summary.averageEstimatedEv())
                )
                .print();
    }

    private void showBookmakers() {
        var report = reportingService.generate(
                ReportFilter.all()
        );

        ConsoleTable table = new ConsoleTable()
                .headers(
                        "Bookmaker",
                        "Bets",
                        "W",
                        "L",
                        "Void",
                        "Stake",
                        "Profit",
                        "ROI",
                        "Avg EV"
                );

        report.bookmakers().forEach(
                row -> table.row(
                        row.bookmakerCode(),
                        row.settledBets(),
                        row.wins(),
                        row.losses(),
                        row.voids(),
                        number(row.settledStakeUnits()),
                        number(row.netProfitUnits()),
                        percent(row.roi()),
                        percent(row.averageEstimatedEv())
                )
        );

        table.print();
    }

    private void showEvBands() {
        var report = reportingService.generate(
                ReportFilter.all()
        );

        ConsoleTable table = new ConsoleTable()
                .headers(
                        "EV band",
                        "Bets",
                        "Stake",
                        "Profit",
                        "ROI"
                );

        report.evBands().forEach(
                row -> table.row(
                        row.evBand(),
                        row.settledBets(),
                        number(row.settledStakeUnits()),
                        number(row.netProfitUnits()),
                        percent(row.roi())
                )
        );

        table.print();
    }

    private void showOpenBets() {
        int limit = input.readInteger(
                "Maximum rows [20]: ",
                20
        );

        ConsoleTable table = new ConsoleTable()
                .headers(
                        "Placed",
                        "Book",
                        "Odds",
                        "Prob.",
                        "EV",
                        "Stake",
                        "Status"
                );

        paperBetRepository.findOpen(limit).forEach(
                bet -> table.row(
                        bet.placedAt(),
                        bet.bookmakerCode(),
                        number(bet.decimalOdds()),
                        percent(bet.probability()),
                        percent(bet.estimatedEv()),
                        number(bet.stakeUnits()),
                        bet.status()
                )
        );

        table.print();
    }

    private void showRecentSettledBets() {
        int limit = input.readInteger(
                "Maximum rows [20]: ",
                20
        );

        ConsoleTable table = new ConsoleTable()
                .headers(
                        "Placed",
                        "Book",
                        "Odds",
                        "EV",
                        "Stake",
                        "Result",
                        "Profit"
                );

        paperBetRepository.findSettled(limit).forEach(
                bet -> table.row(
                        bet.placedAt(),
                        bet.bookmakerCode(),
                        number(bet.decimalOdds()),
                        percent(bet.estimatedEv()),
                        number(bet.stakeUnits()),
                        bet.result(),
                        number(bet.netProfitUnits())
                )
        );

        table.print();
    }

    private void showCumulativeProfit() {
        var report = reportingService.generate(
                ReportFilter.all()
        );

        ConsoleTable table = new ConsoleTable()
                .headers(
                        "#",
                        "Settled at",
                        "Bet profit",
                        "Cumulative"
                );

        report.cumulativeProfit().forEach(
                point -> table.row(
                        point.sequenceNumber(),
                        point.settledAt(),
                        number(point.netProfitUnits()),
                        number(
                                point.cumulativeProfitUnits()
                        )
                )
        );

        table.print();
    }

    private void showMatchingReviewQueue() {
        int limit = input.readInteger(
                "Maximum candidates [20]: ",
                20
        );

        ConsoleTable table = new ConsoleTable()
                .headers(
                        "Score",
                        "Participants",
                        "Prediction participants",
                        "Reason",
                        "Created"
                );

        matchingRepository
                .findReviewQueue(limit)
                .forEach(
                        candidate -> table.row(
                                number(
                                        candidate.overallScore()
                                ),
                                candidate
                                        .sportsbookParticipants(),
                                candidate
                                        .predictionParticipants(),
                                candidate.reason(),
                                candidate.createdAt()
                        )
                );

        table.print();
    }

    private void runKellySimulation() {
        var bets =
                simulationBetRepository.findUsableBets();

        if (bets.isEmpty()) {
            System.out.println(
                    "No settled WIN/LOSS paper bets are available."
            );
            return;
        }

        int runs = input.readInteger(
                "Simulation runs [10000]: ",
                10_000
        );

        int betsPerRun = input.readInteger(
                "Bets per run [500]: ",
                500
        );

        double bankroll = input.readDouble(
                "Starting bankroll [100]: ",
                100.0
        );

        double ruinThreshold = input.readDouble(
                "Practical ruin threshold [10]: ",
                10.0
        );

        var configuration =
                new SimulationConfiguration(
                        bankroll,
                        runs,
                        betsPerRun,
                        42L,
                        ruinThreshold,
                        true
                );

        var flatSummary =
                simulationService.simulate(
                        bets,
                        configuration,
                        new FlatStakeStrategy(1.0)
                );

        var quarterKellySummary =
                simulationService.simulate(
                        bets,
                        configuration,
                        new CappedFractionalKellyStrategy(
                                new KellyFractionCalculator(),
                                0.25,
                                0.02,
                                0.01
                        )
                );

        ConsoleTable table = new ConsoleTable()
                .headers(
                        "Strategy",
                        "Mean ending",
                        "Median",
                        "5th pct.",
                        "95th pct.",
                        "Mean DD",
                        "Ruin",
                        "Profitable"
                );

        table.row(
                flatSummary.strategyCode(),
                decimal(flatSummary.meanEndingBankroll()),
                decimal(flatSummary.medianEndingBankroll()),
                decimal(
                        flatSummary
                                .percentile05EndingBankroll()
                ),
                decimal(
                        flatSummary
                                .percentile95EndingBankroll()
                ),
                percentage(
                        flatSummary
                                .meanMaximumDrawdownFraction()
                ),
                percentage(
                        flatSummary.ruinProbability()
                ),
                percentage(
                        flatSummary
                                .profitableRunProbability()
                )
        );

        table.row(
                quarterKellySummary.strategyCode(),
                decimal(
                        quarterKellySummary
                                .meanEndingBankroll()
                ),
                decimal(
                        quarterKellySummary
                                .medianEndingBankroll()
                ),
                decimal(
                        quarterKellySummary
                                .percentile05EndingBankroll()
                ),
                decimal(
                        quarterKellySummary
                                .percentile95EndingBankroll()
                ),
                percentage(
                        quarterKellySummary
                                .meanMaximumDrawdownFraction()
                ),
                percentage(
                        quarterKellySummary
                                .ruinProbability()
                ),
                percentage(
                        quarterKellySummary
                                .profitableRunProbability()
                )
        );

        table.print();

        if (bets.size() < 100) {
            System.out.println();
            System.out.println(
                    "Warning: only "
                            + bets.size()
                            + " historical bets are available. "
                            + "Results are exploratory."
            );
        }
    }

    private void showDatabaseStatus() {
        var status = databaseRepository.readStatus();

        new ConsoleTable()
                .headers("Database item", "Value")
                .row(
                        "Migration version",
                        status.currentMigrationVersion()
                )
                .row(
                        "Successful migrations",
                        status.successfulMigrations()
                )
                .row(
                        "Application tables",
                        status.applicationTables()
                )
                .row(
                        "PostgreSQL",
                        abbreviate(
                                status.databaseVersion(),
                                90
                        )
                )
                .print();
    }

    private String number(BigDecimal value) {
        if (value == null) {
            return "";
        }

        return value.setScale(
                4,
                RoundingMode.HALF_UP
        ).stripTrailingZeros().toPlainString();
    }

    private String percent(BigDecimal value) {
        if (value == null) {
            return "";
        }

        return value.multiply(
                BigDecimal.valueOf(100)
        ).setScale(
                2,
                RoundingMode.HALF_UP
        ) + "%";
    }

    private String decimal(double value) {
        return String.format("%.2f", value);
    }

    private String percentage(double value) {
        return String.format("%.2f%%", value * 100.0);
    }

    private String abbreviate(
            String value,
            int maximumLength
    ) {
        if (value == null
                || value.length() <= maximumLength) {
            return value;
        }

        return value.substring(
                0,
                maximumLength - 3
        ) + "...";
    }

    private void notImplemented(String operation) {
        System.out.println(
                operation + " has not been connected yet."
        );
    }


    private void runSportsbookIngestion() {
        System.out.println();
        System.out.println(
                "This will contact the configured sportsbook "
                        + "API and write new ingestion data."
        );

        String confirmation = input.readLine(
                "Type RUN to continue: "
        );

        if (!"RUN".equalsIgnoreCase(confirmation)) {
                System.out.println(
                        "Sportsbook ingestion cancelled."
                );
                return;
        }

        System.out.println();
        System.out.println(
                "Running sportsbook ingestion..."
        );

        var result =
                sportsbookIngestionCommand.execute();

        new ConsoleTable()
                .headers("Item", "Value")
                .row(
                        "Successful",
                        result.successful()
                )
                .row(
                        "Ingestion run ID",
                        result.ingestionRunId()
                )
                .row(
                        "Started",
                        result.startedAt()
                )
                .row(
                        "Completed",
                        result.completedAt()
                )
                .row(
                        "Payloads received",
                        result.payloadsReceived()
                )
                .row(
                        "Snapshots stored",
                        result.snapshotsStored()
                )
                .row(
                        "Rejected records",
                        result.rejectedRecords()
                )
                .row(
                        "Message",
                        result.message()
                )
                .print();
        }

        private void runPolymarketIngestion() {
        System.out.println();
        System.out.println(
                "This will contact Polymarket and write new "
                        + "market and order-book data."
        );

        String confirmation = input.readLine(
                "Type RUN to continue: "
        );

        if (!"RUN".equalsIgnoreCase(confirmation)) {
                System.out.println(
                        "Polymarket ingestion cancelled."
                );
                return;
        }

        System.out.println();
        System.out.println(
                "Running Polymarket ingestion..."
        );

        PolymarketIngestionResult result =
                polymarketIngestionCommand.execute();

        new ConsoleTable()
                .headers("Item", "Value")
                .row(
                        "Successful",
                        result.successful()
                )
                .row(
                        "Ingestion run ID",
                        result.ingestionRunId()
                )
                .row(
                        "Started",
                        result.startedAt()
                )
                .row(
                        "Completed",
                        result.completedAt()
                )
                .row(
                        "Markets received",
                        result.marketsReceived()
                )
                .row(
                        "Order books received",
                        result.orderBooksReceived()
                )
                .row(
                        "Snapshots stored",
                        result.snapshotsStored()
                )
                .row(
                        "Rejected records",
                        result.rejectedRecords()
                )
                .row(
                        "Message",
                        result.message()
                )
                .print();
        }

        private void runAutomatedMatching() {
                System.out.println();
                System.out.println(
                        "This will compare eligible sportsbook and "
                                + "Polymarket markets and store matching candidates."
                );

                String confirmation = input.readLine(
                        "Type RUN to continue: "
                );

                if (!"RUN".equalsIgnoreCase(confirmation)) {
                        System.out.println(
                                "Automated matching cancelled."
                        );
                        return;
                }

                System.out.println();
                System.out.println(
                        "Running automated matching..."
                );

                AutomatedMatchingConsoleResult result =
                        automatedMatchingCommand.execute();

                new ConsoleTable()
                        .headers("Item", "Value")
                        .row(
                                "Successful",
                                result.successful()
                        )
                        .row(
                                "Matching run ID",
                                result.runId()
                        )
                        .row(
                                "Started",
                                result.startedAt()
                        )
                        .row(
                                "Completed",
                                result.completedAt()
                        )
                        .row(
                                "Candidates evaluated",
                                result.candidatesEvaluated()
                        )
                        .row(
                                "Automatically approved",
                                result.autoApproved()
                        )
                        .row(
                                "Review required",
                                result.reviewRequired()
                        )
                        .row(
                                "Rejected",
                                result.rejected()
                        )
                        .row(
                                "Superseded",
                                result.superseded()
                        )
                        .row(
                                "Message",
                                result.message()
                        )
                        .print();
                }
        
        private void reviewMappings() {
                boolean reviewing = true;

                while (reviewing) {
                        var candidateOptional =
                                mappingReviewService
                                        .findNextReviewCandidate();

                        if (candidateOptional.isEmpty()) {
                        System.out.println();
                        System.out.println(
                                "There are no mappings pending review."
                        );
                        return;
                        }

                        MappingReviewCandidate candidate =
                                candidateOptional.get();

                        printReviewCandidate(candidate);

                        System.out.println();
                        System.out.println("1. Approve mapping");
                        System.out.println("2. Reject mapping");
                        System.out.println("3. Skip and return");
                        System.out.println();

                        String selection = input.readLine(
                                "Selection: "
                        );

                        switch (selection.trim()) {
                        case "1" -> approveCandidate(candidate);
                        case "2" -> rejectCandidate(candidate);
                        case "3" -> reviewing = false;
                        default -> System.out.println(
                                "Unknown selection."
                        );
                        }
                }
        }

        private void printReviewCandidate(
                        MappingReviewCandidate candidate
                ) {
                System.out.println();
                System.out.println(
                        "========================================"
                );
                System.out.println("MAPPING REVIEW");
                System.out.println(
                        "========================================"
                );

                new ConsoleTable()
                        .headers("Item", "Value")
                        .row(
                                "Candidate ID",
                                candidate.candidateId()
                        )
                        .row(
                                "Sportsbook market",
                                candidate.sportsbookMarketName()
                        )
                        .row(
                                "Polymarket market",
                                candidate.predictionMarketName()
                        )
                        .row(
                                "Participant score",
                                candidate.participantScore()
                        )
                        .row(
                                "Time score",
                                candidate.scheduledTimeScore()
                        )
                        .row(
                                "Overall score",
                                candidate.overallScore()
                        )
                        .row(
                                "Automated reason",
                                candidate.decisionReason()
                        )
                        .print();

                System.out.println();
                System.out.println("Sportsbook outcomes:");

                printOutcomes(candidate.sportsbookOutcomes());

                System.out.println();
                System.out.println("Polymarket outcomes:");

                printOutcomes(candidate.predictionOutcomes());
        }
        
        private void printOutcomes(
                java.util.List<MappingReviewOutcome> outcomes
        ) {
                for (int index = 0; index < outcomes.size(); index++) {
                        MappingReviewOutcome outcome =
                                outcomes.get(index);

                        System.out.printf(
                                "%d. %s [%s]%n",
                                index + 1,
                                outcome.outcomeName(),
                                outcome.normalizedOutcomeName()
                        );
                }
        }

        private void approveCandidate(
                MappingReviewCandidate candidate
        ) {
                if (candidate.sportsbookOutcomes().size() != 2
                        || candidate.predictionOutcomes().size() != 2) {

                        System.out.println(
                                "Approval requires exactly two participant "
                                        + "outcomes on each market."
                        );
                        return;
                }

                String firstSelectionText = input.readLine(
                        "Polymarket outcome number matching sportsbook "
                                + "outcome 1: "
                );

                int firstPredictionIndex;

                try {
                        firstPredictionIndex =
                                Integer.parseInt(
                                        firstSelectionText.trim()
                                ) - 1;
                } catch (NumberFormatException exception) {
                        System.out.println(
                                "Invalid outcome number."
                        );
                        return;
                }

                if (firstPredictionIndex < 0
                        || firstPredictionIndex >= 2) {
                        System.out.println(
                                "Outcome number must be 1 or 2."
                        );
                        return;
                }

                int secondPredictionIndex =
                        firstPredictionIndex == 0 ? 1 : 0;

                MappingReviewOutcome sportsbookFirst =
                        candidate.sportsbookOutcomes().get(0);

                MappingReviewOutcome sportsbookSecond =
                        candidate.sportsbookOutcomes().get(1);

                MappingReviewOutcome predictionFirst =
                        candidate.predictionOutcomes().get(
                                firstPredictionIndex
                        );

                MappingReviewOutcome predictionSecond =
                        candidate.predictionOutcomes().get(
                                secondPredictionIndex
                        );

                System.out.println();
                System.out.println("Proposed mappings:");
                System.out.println(
                        sportsbookFirst.outcomeName()
                                + " -> "
                                + predictionFirst.outcomeName()
                );
                System.out.println(
                        sportsbookSecond.outcomeName()
                                + " -> "
                                + predictionSecond.outcomeName()
                );

                String confirmation = input.readLine(
                        "Type APPROVE to confirm: "
                );

                if (!"APPROVE".equalsIgnoreCase(
                        confirmation.trim()
                )) {
                        System.out.println(
                                "Approval cancelled."
                        );
                        return;
                }

                String reviewer = input.readLine(
                        "Reviewer name: "
                );

                MappingReviewResult result =
                        mappingReviewService.approve(
                                candidate.candidateId(),
                                sportsbookFirst.outcomeId(),
                                predictionFirst.outcomeId(),
                                sportsbookSecond.outcomeId(),
                                predictionSecond.outcomeId(),
                                reviewer
                        );

                System.out.println(result.message());

                if (result.successful()) {
                        System.out.println(
                                "Created mapping IDs: "
                                        + result.createdMappingIds()
                        );
                }
        }

        private void rejectCandidate(
                MappingReviewCandidate candidate
        ) {
                String reason = input.readLine(
                        "Rejection reason: "
                );

                String reviewer = input.readLine(
                        "Reviewer name: "
                );

                String confirmation = input.readLine(
                        "Type REJECT to confirm: "
                );

                if (!"REJECT".equalsIgnoreCase(
                        confirmation.trim()
                )) {
                        System.out.println(
                                "Rejection cancelled."
                        );
                        return;
                }

                MappingReviewResult result =
                        mappingReviewService.reject(
                                candidate.candidateId(),
                                reviewer,
                                reason
                        );

                System.out.println(result.message());
        }

        private void calculateOpportunities() {
                System.out.println();
                System.out.println(
                        "This will calculate and store opportunities "
                                + "using the active UFC_EV strategy."
                );

                String confirmation = input.readLine(
                        "Type RUN to continue: "
                );

                if (!"RUN".equalsIgnoreCase(
                        confirmation.trim()
                )) {
                        System.out.println(
                                "Opportunity calculation cancelled."
                        );
                        return;
                }

                System.out.println();
                System.out.println(
                        "Calculating opportunities..."
                );

                OpportunityCalculationResult result =
                        opportunityCalculationCommand.execute();

                new ConsoleTable()
                        .headers("Item", "Value")
                        .row(
                                "Successful",
                                result.successful()
                        )
                        .row(
                                "Strategy version",
                                result.strategyVersionId()
                        )
                        .row(
                                "Started",
                                result.startedAt()
                        )
                        .row(
                                "Completed",
                                result.completedAt()
                        )
                        .row(
                                "Approved mappings",
                                result.approvedMappings()
                        )
                        .row(
                                "Mappings with snapshots",
                                result.mappingsWithSnapshots()
                        )
                        .row(
                                "Qualified",
                                result.qualified()
                        )
                        .row(
                                "Rejected",
                                result.rejected()
                        )
                        .row(
                                "Duplicates skipped",
                                result.duplicatesSkipped()
                        )
                        .row(
                                "Message",
                                result.message()
                        )
                        .print();
                }

        private void placeQualifiedPaperBets() {
                System.out.println();
                System.out.println(
                        "This will create flat one-unit paper bets "
                                + "for all currently qualified opportunities."
                );

                System.out.println(
                        "The first qualifying entry per fighter, "
                                + "bookmaker, and fight becomes the "
                                + "official paper bet."
                );

                String confirmation = input.readLine(
                        "Type PLACE to continue: "
                );

                if (!"PLACE".equalsIgnoreCase(
                        confirmation.trim()
                )) {
                        System.out.println(
                                "Paper-bet placement cancelled."
                        );
                        return;
                }

                System.out.println();
                System.out.println(
                        "Placing qualified paper bets..."
                );

                QualifiedPaperBetPlacementResult result =
                        qualifiedPaperBetPlacementCommand.execute();

                new ConsoleTable()
                        .headers("Item", "Value")
                        .row(
                                "Successful",
                                result.successful()
                        )
                        .row(
                                "Started",
                                result.startedAt()
                        )
                        .row(
                                "Completed",
                                result.completedAt()
                        )
                        .row(
                                "Qualified opportunities",
                                result.qualifiedOpportunitiesFound()
                        )
                        .row(
                                "Paper bets placed",
                                result.placed()
                        )
                        .row(
                                "Already placed",
                                result.alreadyPlaced()
                        )
                        .row(
                                "Official entry already exists",
                                result.officialEntryAlreadyExists()
                        )
                        .row(
                                "Failed",
                                result.failed()
                        )
                        .row(
                                "Message",
                                result.message()
                        )
                        .print();
                }
        
        private void recordEventResolution() {
                var events =
                        eventResolutionRecordingService
                                .findResolvableEvents();

                if (events.isEmpty()) {
                        System.out.println();
                        System.out.println(
                                "There are no unresolved events with "
                                        + "open paper bets."
                        );
                        return;
                }

                System.out.println();
                System.out.println(
                        "========================================"
                );
                System.out.println("RECORD EVENT RESOLUTION");
                System.out.println(
                        "========================================"
                );

                for (int index = 0; index < events.size(); index++) {
                        ResolvableEvent event = events.get(index);

                        System.out.printf(
                                "%d. %s | %s | status=%s | open bets=%d%n",
                                index + 1,
                                event.eventName(),
                                event.scheduledStartTime(),
                                event.eventStatus(),
                                event.openPaperBetCount()
                        );
                }

                System.out.println("0. Cancel");

                Integer eventIndex = readNumber(
                        "Select event: ",
                        0,
                        events.size()
                );

                if (eventIndex == null || eventIndex == 0) {
                        System.out.println(
                                "Resolution recording cancelled."
                        );
                        return;
                }

                ResolvableEvent event =
                        events.get(eventIndex - 1);

                recordResolutionFor(event);
        }

        private void recordResolutionFor(
                ResolvableEvent event
        ) {
        System.out.println();
        System.out.println(
                "Event: " + event.eventName()
        );

        for (int index = 0;
                index < event.participants().size();
                index++) {

                ResolvableParticipant participant =
                        event.participants().get(index);

                System.out.printf(
                        "%d. %s wins%n",
                        index + 1,
                        participant.participantName()
                );
        }

        int drawOption =
                event.participants().size() + 1;

        int noContestOption =
                event.participants().size() + 2;

        int cancelledOption =
                event.participants().size() + 3;

        int postponedOption =
                event.participants().size() + 4;

        System.out.println(drawOption + ". Draw");
        System.out.println(
                noContestOption + ". No contest / void"
        );
        System.out.println(
                cancelledOption + ". Cancelled"
        );
        System.out.println(
                postponedOption + ". Postponed"
        );
        System.out.println("0. Cancel");

        Integer resultSelection = readNumber(
                "Select official result: ",
                0,
                postponedOption
        );

        if (resultSelection == null
                || resultSelection == 0) {
                System.out.println(
                        "Resolution recording cancelled."
                );
                return;
        }

        String resultText = input.readLine(
                "Official result description: "
        );

        String recordedBy = input.readLine(
                "Recorded by: "
        );

        printResolutionConfirmation(
                event,
                resultSelection,
                drawOption,
                noContestOption,
                cancelledOption,
                postponedOption
        );

        String confirmation = input.readLine(
                "Type RECORD to confirm: "
        );

        if (!"RECORD".equalsIgnoreCase(
                confirmation.trim()
        )) {
                System.out.println(
                        "Resolution recording cancelled."
                );
                return;
        }

        RecordResolutionResult result;

        if (resultSelection <=
                event.participants().size()) {

                ResolvableParticipant winner =
                        event.participants().get(
                                resultSelection - 1
                        );

                result =
                        eventResolutionRecordingService
                                .recordParticipantWin(
                                        event.eventId(),
                                        winner.participantId(),
                                        resultText,
                                        recordedBy
                                );
        } else {
                String resultType =
                        resultSelection == drawOption
                                ? "DRAW"
                                : resultSelection ==
                                noContestOption
                                ? "NO_CONTEST"
                                : resultSelection ==
                                cancelledOption
                                ? "CANCELLED"
                                : "POSTPONED";

                result =
                        eventResolutionRecordingService
                                .recordNonWinnerResult(
                                        event.eventId(),
                                        resultType,
                                        resultText,
                                        recordedBy
                                );
        }

        new ConsoleTable()
                .headers("Item", "Value")
                .row(
                        "Successful",
                        result.successful()
                )
                .row(
                        "Resolution ID",
                        result.resolutionId()
                )
                .row(
                        "Message",
                        result.message()
                )
                .print();
        }

        private void printResolutionConfirmation(
                ResolvableEvent event,
                int selection,
                int drawOption,
                int noContestOption,
                int cancelledOption,
                int postponedOption
        ) {
        String resultDescription;

        if (selection <= event.participants().size()) {
                resultDescription =
                        event.participants()
                                .get(selection - 1)
                                .participantName()
                                + " wins";
        } else if (selection == drawOption) {
                resultDescription = "Draw";
        } else if (selection == noContestOption) {
                resultDescription = "No contest / void";
        } else if (selection == cancelledOption) {
                resultDescription = "Cancelled";
        } else if (selection == postponedOption) {
                resultDescription = "Postponed";
        } else {
                resultDescription = "Unknown";
        }

        System.out.println();
        System.out.println(
                "Event:  " + event.eventName()
        );
        System.out.println(
                "Result: " + resultDescription
        );
        }

        private Integer readNumber(
                String prompt,
                int minimum,
                int maximum
        ) {
                String value = input.readLine(prompt);

                try {
                        int parsed = Integer.parseInt(
                                value.trim()
                        );

                        if (parsed < minimum || parsed > maximum) {
                        System.out.printf(
                                "Enter a number from %d to %d.%n",
                                minimum,
                                maximum
                        );
                        return null;
                        }

                        return parsed;
                } catch (NumberFormatException exception) {
                        System.out.println(
                                "Enter a valid number."
                        );
                        return null;
                }
        }

        private void settleCompletedEvents() {
                List<SettleableEvent> events =
                        completedEventSettlementCommand
                                .findEligibleEvents();

                if (events.isEmpty()) {
                        System.out.println();
                        System.out.println(
                                "There are no completed events with "
                                        + "open paper bets."
                        );
                        return;
                }

                System.out.println();
                System.out.println(
                        "========================================"
                );
                System.out.println("SETTLE COMPLETED EVENTS");
                System.out.println(
                        "========================================"
                );

                for (SettleableEvent event : events) {
                        System.out.printf(
                                "%s | result=%s | open bets=%d%n",
                                event.eventName(),
                                event.officialResultType(),
                                event.openPaperBetCount()
                        );
                }

                System.out.println();
                System.out.printf(
                        "Eligible events: %d%n",
                        events.size()
                );

                String resolvedBy = input.readLine(
                        "Settled by: "
                );

                String confirmation = input.readLine(
                        "Type SETTLE ALL to confirm: "
                );

                if (!"SETTLE ALL".equalsIgnoreCase(
                        confirmation.trim()
                )) {
                        System.out.println(
                                "Settlement cancelled."
                        );
                        return;
                }

                CompletedEventSettlementResult result =
                        completedEventSettlementCommand
                                .settleAll(resolvedBy);

                new ConsoleTable()
                        .headers("Item", "Value")
                        .row(
                                "Successful",
                                result.successful()
                        )
                        .row(
                                "Started",
                                result.startedAt()
                        )
                        .row(
                                "Completed",
                                result.completedAt()
                        )
                        .row(
                                "Eligible events",
                                result.eligibleEvents()
                        )
                        .row(
                                "Events settled",
                                result.eventsSettled()
                        )
                        .row(
                                "Bets settled",
                                result.betsSettled()
                        )
                        .row(
                                "Failed events",
                                result.failedEvents()
                        )
                        .row(
                                "Message",
                                result.message()
                        )
                        .print();
                }
}