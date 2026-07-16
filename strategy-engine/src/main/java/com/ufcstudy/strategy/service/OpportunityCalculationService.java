package com.ufcstudy.strategy.service;

import com.ufcstudy.calculation.ExpectedValueCalculator;
import com.ufcstudy.calculation.ImpliedProbabilityCalculator;
import com.ufcstudy.calculation.TwoWayNoVigCalculator;
import com.ufcstudy.domain.strategy.OpportunityRejectionCode;
import com.ufcstudy.domain.strategy.OpportunityStatus;
import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.domain.value.Probability;
import com.ufcstudy.domain.value.Units;
import com.ufcstudy.persistence.opportunity.model.OpportunityCandidateRecord;
import com.ufcstudy.persistence.opportunity.repository.OpportunityCandidateRepository;
import com.ufcstudy.strategy.calculation.OpportunityTimingCalculator;
import com.ufcstudy.strategy.calculation.ReferenceProbabilitySelector;
import com.ufcstudy.strategy.model.OpportunityEvaluationRequest;
import com.ufcstudy.strategy.model.OpportunityEvaluationResult;
import com.ufcstudy.strategy.model.RuleFailure;
import com.ufcstudy.strategy.model.StrategyRules;
import com.ufcstudy.strategy.validation.OpportunityRuleEvaluator;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

public final class OpportunityCalculationService {

    private final OpportunityCandidateRepository candidateRepository;
    private final ReferenceProbabilitySelector probabilitySelector;
    private final ImpliedProbabilityCalculator impliedCalculator;
    private final TwoWayNoVigCalculator noVigCalculator;
    private final ExpectedValueCalculator expectedValueCalculator;
    private final OpportunityTimingCalculator timingCalculator;
    private final OpportunityRuleEvaluator ruleEvaluator;
    private final StrategyRules rules;
    private final Clock clock;

    public OpportunityCalculationService(
            OpportunityCandidateRepository candidateRepository,
            ReferenceProbabilitySelector probabilitySelector,
            ImpliedProbabilityCalculator impliedCalculator,
            TwoWayNoVigCalculator noVigCalculator,
            ExpectedValueCalculator expectedValueCalculator,
            OpportunityTimingCalculator timingCalculator,
            OpportunityRuleEvaluator ruleEvaluator,
            StrategyRules rules,
            Clock clock
    ) {
        this.candidateRepository =
                Objects.requireNonNull(candidateRepository);
        this.probabilitySelector =
                Objects.requireNonNull(probabilitySelector);
        this.impliedCalculator =
                Objects.requireNonNull(impliedCalculator);
        this.noVigCalculator =
                Objects.requireNonNull(noVigCalculator);
        this.expectedValueCalculator =
                Objects.requireNonNull(expectedValueCalculator);
        this.timingCalculator =
                Objects.requireNonNull(timingCalculator);
        this.ruleEvaluator =
                Objects.requireNonNull(ruleEvaluator);
        this.rules = Objects.requireNonNull(rules);
        this.clock = Objects.requireNonNull(clock);
    }

    public OpportunityEvaluationResult calculate(
            OpportunityEvaluationRequest request
    ) {
        OpportunityCandidateRecord candidate =
                candidateRepository.findCandidate(
                        request.marketMappingId(),
                        request.sportsbookSnapshotId(),
                        request.predictionSnapshotId()
                ).orElseThrow(
                        () -> new IllegalArgumentException(
                                "No complete opportunity candidate exists "
                                        + "for the supplied mapping and snapshots"
                        )
                );

        var decisionTime = clock.instant();

        BigDecimal referenceProbabilityValue =
                probabilitySelector.select(
                        rules.probabilityMethod(),
                        candidate
                );

        Probability referenceProbability =
                Probability.of(referenceProbabilityValue);

        DecimalOdds selectedOdds =
                DecimalOdds.of(
                        candidate.sportsbookDecimalOdds()
                );

        DecimalOdds opposingOdds =
                DecimalOdds.of(
                        candidate.opposingSportsbookDecimalOdds()
                );

        Probability rawImplied =
                impliedCalculator.calculate(selectedOdds);

        var noVig = noVigCalculator.calculate(
                selectedOdds,
                opposingOdds
        );

        var evResult = expectedValueCalculator.calculate(
                referenceProbability,
                selectedOdds,
                Units.of("1")
        );

        var timing = timingCalculator.calculate(
                candidate,
                decisionTime
        );

        List<RuleFailure> failures =
                ruleEvaluator.evaluate(
                        candidate,
                        rules,
                        timing,
                        referenceProbabilityValue,
                        evResult.expectedReturnRate()
                );

        OpportunityStatus status = failures.isEmpty()
                ? OpportunityStatus.QUALIFIED
                : OpportunityStatus.REJECTED;

        OpportunityRejectionCode primaryCode =
                failures.isEmpty()
                        ? null
                        : failures.getFirst().code();

        return new OpportunityEvaluationResult(
                rules.strategyVersionId(),
                candidate.marketMappingId(),
                candidate.sportsbookSnapshotId(),
                candidate.opposingSportsbookSnapshotId(),
                candidate.predictionSnapshotId(),
                decisionTime,
                rules.probabilityMethod(),
                referenceProbabilityValue,
                candidate.sportsbookDecimalOdds(),
                rawImplied.value(),
                noVig.noVigProbabilityA().value(),
                evResult.expectedReturnRate(),
                evResult.expectedProfitUnits(),
                candidate.predictionSpread(),
                timing.sportsbookSnapshotAgeSeconds(),
                timing.predictionSnapshotAgeSeconds(),
                timing.sourceGapSeconds(),
                timing.secondsUntilScheduledStart(),
                status,
                primaryCode,
                failures,
                rules.calculationVersion()
        );
    }
}