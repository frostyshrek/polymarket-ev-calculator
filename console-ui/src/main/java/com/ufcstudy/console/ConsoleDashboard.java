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
import java.util.Objects;

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

    private final ConsoleInput input = new ConsoleInput();

    public ConsoleDashboard(
            StudyReportingService reportingService,
            PaperBetConsoleRepository paperBetRepository,
            MatchingReviewRepository matchingRepository,
            DatabaseStatusRepository databaseRepository,
            HistoricalSimulationBetRepository simulationBetRepository,
            KellySimulationService simulationService,
            ConfigurableApplicationContext context
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
                Objects.requireNonNull(
                        simulationBetRepository
                );
        this.simulationService =
                Objects.requireNonNull(simulationService);
        this.context = Objects.requireNonNull(context);
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
                    case "1" -> showSummary();
                    case "2" -> showBookmakers();
                    case "3" -> showEvBands();
                    case "4" -> showOpenBets();
                    case "5" -> showRecentSettledBets();
                    case "6" -> showCumulativeProfit();
                    case "7" -> showMatchingReviewQueue();
                    case "8" -> runKellySimulation();
                    case "9" -> showDatabaseStatus();
                    case "0" -> running = false;
                    default ->
                            System.out.println(
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
        System.out.println("1. Study summary");
        System.out.println("2. Performance by bookmaker");
        System.out.println("3. Performance by EV band");
        System.out.println("4. Open paper bets");
        System.out.println("5. Recent settled bets");
        System.out.println("6. Cumulative profit");
        System.out.println("7. Automated-match review queue");
        System.out.println("8. Run Kelly simulation");
        System.out.println("9. Database status");
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
}