package com.ufcstudy.console.operation;

import com.ufcstudy.eventmatching.automated.AutomatedMatchingResult;
import com.ufcstudy.eventmatching.automated.AutomatedMatchingService;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class DefaultAutomatedMatchingCommand
        implements AutomatedMatchingCommand {

    private final AutomatedMatchingCandidateProvider candidateProvider;
    private final AutomatedMatchingService matchingService;
    private final Clock clock;

    public DefaultAutomatedMatchingCommand(
            AutomatedMatchingCandidateProvider candidateProvider,
            AutomatedMatchingService matchingService,
            Clock clock
    ) {
        this.candidateProvider =
                Objects.requireNonNull(candidateProvider);
        this.matchingService =
                Objects.requireNonNull(matchingService);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public AutomatedMatchingConsoleResult execute() {
        Instant startedAt = clock.instant();

        try {
            var candidates =
                    candidateProvider.findCandidates();

            AutomatedMatchingResult result =
                    matchingService.process(candidates);

            return new AutomatedMatchingConsoleResult(
                    result.runId(),
                    startedAt,
                    clock.instant(),
                    result.candidatesEvaluated(),
                    result.autoApproved(),
                    result.reviewRequired(),
                    result.rejected(),
                    result.superseded(),
                    true,
                    candidates.isEmpty()
                            ? "No eligible market pairs were found."
                            : "Automated matching completed successfully."
            );
        } catch (RuntimeException exception) {
            return new AutomatedMatchingConsoleResult(
                    null,
                    startedAt,
                    clock.instant(),
                    0,
                    0,
                    0,
                    0,
                    0,
                    false,
                    errorMessage(exception)
            );
        }
    }

    private static String errorMessage(
            RuntimeException exception
    ) {
        String message = exception.getMessage();

        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}