package com.ufcstudy.paperbetting.service;

import com.ufcstudy.domain.paperbetting.PaperBetStatus;
import com.ufcstudy.domain.paperbetting.StakeMethod;
import com.ufcstudy.persistence.paperbetting.model.PaperBetCandidateRecord;
import com.ufcstudy.persistence.paperbetting.model.PaperBetInsert;
import com.ufcstudy.persistence.paperbetting.repository.PaperBetCandidateRepository;
import com.ufcstudy.persistence.paperbetting.repository.PaperBetRepository;
import com.ufcstudy.paperbetting.model.PaperBetPlacementRequest;
import com.ufcstudy.paperbetting.model.PaperBetPlacementResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class PaperBetPlacementService {

    private static final BigDecimal PRIMARY_STAKE =
            new BigDecimal("1.00000000");

    private final TransactionTemplate transactions;
    private final PaperBetCandidateRepository candidateRepository;
    private final PaperBetRepository paperBetRepository;

    public PaperBetPlacementService(
            TransactionTemplate transactions,
            PaperBetCandidateRepository candidateRepository,
            PaperBetRepository paperBetRepository
    ) {
        this.transactions = Objects.requireNonNull(transactions);
        this.candidateRepository =
                Objects.requireNonNull(candidateRepository);
        this.paperBetRepository =
                Objects.requireNonNull(paperBetRepository);
    }

    public PaperBetPlacementResult place(
            PaperBetPlacementRequest request
    ) {
        Objects.requireNonNull(request);

        return transactions.execute(status -> {
            PaperBetCandidateRecord candidate =
                    candidateRepository
                            .findByOpportunityId(
                                    request.opportunityId()
                            )
                            .orElseThrow(
                                    () -> new PaperBetPlacementException(
                                            "Opportunity does not exist: "
                                                    + request
                                                    .opportunityId()
                                    )
                            );

            validateCandidate(candidate);

            if (paperBetRepository.existsForOpportunity(
                    candidate.opportunityId()
            )) {
                return PaperBetPlacementResult.alreadyPlaced(
                        candidate.opportunityId()
                );
            }

            String stakeMethod =
                    StakeMethod.FLAT_ONE_UNIT.name();

            if (paperBetRepository.officialEntryExists(
                    candidate.strategyVersionId(),
                    candidate.sportingEventId(),
                    candidate.sportsbookOutcomeId(),
                    candidate.bookmakerCode(),
                    stakeMethod
            )) {
                return PaperBetPlacementResult
                        .officialEntryExists(
                                candidate.opportunityId()
                        );
            }

            UUID paperBetId = UUID.randomUUID();

            paperBetRepository.insert(
                    new PaperBetInsert(
                            paperBetId,
                            candidate.strategyVersionId(),
                            candidate.opportunityId(),
                            candidate.sportingEventId(),
                            candidate.marketMappingId(),
                            candidate.sportsbookMarketId(),
                            candidate.sportsbookOutcomeId(),
                            candidate
                                    .predictionMarketOutcomeId(),
                            candidate.bookmakerCode(),
                            candidate.decisionTime(),
                            candidate.sportsbookDecimalOdds(),
                            candidate.referenceProbability(),
                            candidate.estimatedExpectedValue(),
                            stakeMethod,
                            PRIMARY_STAKE,
                            PaperBetStatus.OPEN.name()
                    )
            );

            return PaperBetPlacementResult.placed(
                    paperBetId,
                    candidate.opportunityId(),
                    PRIMARY_STAKE
            );
        });
    }

    private static void validateCandidate(
            PaperBetCandidateRecord candidate
    ) {
        if (!"QUALIFIED".equals(
                candidate.qualificationStatus()
        )) {
            throw new PaperBetPlacementException(
                    "Only QUALIFIED opportunities can create paper bets"
            );
        }

        if (!"APPROVED_MANUAL".equals(
                candidate.mappingStatus()
        ) && !"APPROVED_AUTOMATIC".equals(
                candidate.mappingStatus()
        )) {
            throw new PaperBetPlacementException(
                    "Opportunity mapping is not approved"
            );
        }

        if (!"EXACT".equals(
                candidate.settlementCompatibility()
        )) {
            throw new PaperBetPlacementException(
                    "Settlement compatibility must be EXACT"
            );
        }

        if (candidate.sportsbookDecimalOdds()
                .compareTo(BigDecimal.ONE) <= 0) {
            throw new PaperBetPlacementException(
                    "Sportsbook decimal odds must exceed 1"
            );
        }

        if (candidate.referenceProbability().signum() <= 0
                || candidate.referenceProbability()
                .compareTo(BigDecimal.ONE) >= 0) {
            throw new PaperBetPlacementException(
                    "Reference probability must be between zero and one"
            );
        }

        if (candidate.estimatedExpectedValue().signum() < 0) {
            throw new PaperBetPlacementException(
                    "Qualified opportunity cannot have negative EV"
            );
        }
    }
}