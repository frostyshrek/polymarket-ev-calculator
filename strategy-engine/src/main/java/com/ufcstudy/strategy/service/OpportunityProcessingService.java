package com.ufcstudy.strategy.service;

import com.ufcstudy.domain.strategy.OpportunityRejectionCode;
import com.ufcstudy.persistence.opportunity.model.OpportunityInsert;
import com.ufcstudy.persistence.opportunity.repository.OpportunityRepository;
import com.ufcstudy.strategy.model.OpportunityEvaluationRequest;
import com.ufcstudy.strategy.model.OpportunityEvaluationResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;
import java.util.UUID;

public final class OpportunityProcessingService {

    private final TransactionTemplate transactions;
    private final OpportunityCalculationService calculationService;
    private final OpportunityRepository opportunityRepository;

    public OpportunityProcessingService(
            TransactionTemplate transactions,
            OpportunityCalculationService calculationService,
            OpportunityRepository opportunityRepository
    ) {
        this.transactions = Objects.requireNonNull(transactions);
        this.calculationService =
                Objects.requireNonNull(calculationService);
        this.opportunityRepository =
                Objects.requireNonNull(opportunityRepository);
    }

    public UUID process(OpportunityEvaluationRequest request) {
        return transactions.execute(status -> {
            OpportunityEvaluationResult result =
                    calculationService.calculate(request);

            if (opportunityRepository.existsForSnapshotPair(
                    result.strategyVersionId(),
                    result.marketMappingId(),
                    result.sportsbookSnapshotId(),
                    result.predictionSnapshotId()
            )) {
                throw new IllegalStateException(
                        OpportunityRejectionCode
                                .DUPLICATE_OFFICIAL_OPPORTUNITY
                                .name()
                );
            }

            UUID opportunityId = UUID.randomUUID();

            opportunityRepository.insert(
                    new OpportunityInsert(
                            opportunityId,
                            result.strategyVersionId(),
                            result.marketMappingId(),
                            result.sportsbookSnapshotId(),
                            result.opposingSportsbookSnapshotId(),
                            result.predictionSnapshotId(),
                            result.decisionTime(),
                            result.referenceProbability(),
                            result.probabilityMethod().name(),
                            result.sportsbookDecimalOdds(),
                            result.rawImpliedProbability(),
                            result.sportsbookNoVigProbability(),
                            result.estimatedExpectedValue(),
                            result.expectedProfitPerUnit(),
                            result.predictionMarketSpread(),
                            result.sportsbookSnapshotAgeSeconds(),
                            result.predictionSnapshotAgeSeconds(),
                            result.sourceGapSeconds(),
                            result.secondsUntilScheduledStart(),
                            result.status().name(),
                            result.qualificationReason(),
                            result.primaryRejectionCode() == null
                                    ? null
                                    : result
                                    .primaryRejectionCode()
                                    .name(),
                            result.calculationVersion()
                    )
            );

            return opportunityId;
        });
    }
}