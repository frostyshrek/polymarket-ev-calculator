package com.ufcstudy.eventmatching.automated;

import com.ufcstudy.eventmatching.automated.persistence.AutomatedMatchCandidateInsert;
import com.ufcstudy.eventmatching.automated.persistence.AutomatedMatchCandidateRepository;
import com.ufcstudy.eventmatching.automated.persistence.AutomatedMatchRunRepository;
import com.ufcstudy.eventmatching.automated.persistence.ExistingMarketMappingRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class AutomatedMatchingService {

    private final AutomatedMatchScorer scorer;
    private final AutomatedMatchDecisionPolicy decisionPolicy;
    private final FighterNameNormalizer normalizer;
    private final AutomatedMatchRunRepository runRepository;
    private final AutomatedMatchCandidateRepository candidateRepository;
    private final ExistingMarketMappingRepository existingMappingRepository;
    private final Clock clock;

    public AutomatedMatchingService(
            AutomatedMatchScorer scorer,
            AutomatedMatchDecisionPolicy decisionPolicy,
            FighterNameNormalizer normalizer,
            AutomatedMatchRunRepository runRepository,
            AutomatedMatchCandidateRepository candidateRepository,
            ExistingMarketMappingRepository existingMappingRepository,
            Clock clock
    ) {
        this.scorer = Objects.requireNonNull(scorer);
        this.decisionPolicy =
                Objects.requireNonNull(decisionPolicy);
        this.normalizer = Objects.requireNonNull(normalizer);
        this.runRepository =
                Objects.requireNonNull(runRepository);
        this.candidateRepository =
                Objects.requireNonNull(candidateRepository);
        this.existingMappingRepository =
                Objects.requireNonNull(
                        existingMappingRepository
                );
        this.clock = Objects.requireNonNull(clock);
    }

    public AutomatedMatchingResult process(
            List<AutomatedMarketCandidate> candidates
    ) {
        Objects.requireNonNull(candidates);

        Instant startedAt = clock.instant();
        UUID runId = runRepository.start(startedAt);

        int autoApproved = 0;
        int reviewRequired = 0;
        int rejected = 0;
        int superseded = 0;

        try {
            for (AutomatedMarketCandidate candidate
                    : candidates) {
                AutomatedMatchScore score =
                        scorer.score(candidate);

                AutomatedMatchDecision decision;

                if (existingMappingRepository.mappingExists(
                        candidate.sportsbookMarketId(),
                        candidate.predictionMarketId()
                )) {
                    decision =
                            new AutomatedMatchDecision(
                                    AutomatedMatchStatus
                                            .SUPERSEDED,
                                    "MARKET_ALREADY_MAPPED"
                            );
                } else {
                    decision =
                            decisionPolicy.decide(
                                    candidate,
                                    score
                            );
                }

                switch (decision.status()) {
                    case AUTO_APPROVED -> autoApproved++;
                    case REVIEW_REQUIRED -> reviewRequired++;
                    case REJECTED -> rejected++;
                    case SUPERSEDED -> superseded++;
                }

                candidateRepository.insert(
                        new AutomatedMatchCandidateInsert(
                                UUID.randomUUID(),
                                runId,
                                candidate.sportsbookMarketId(),
                                candidate.predictionMarketId(),
                                decimal(
                                        score.participantScore()
                                ),
                                decimal(
                                        score.scheduledTimeScore()
                                ),
                                decimal(
                                        score.overallScore()
                                ),
                                score.scheduledTimeDifference()
                                        .toSeconds(),
                                decision.status(),
                                decision.reason(),
                                candidate
                                        .sportsbookParticipants()
                                        .normalizedKey(normalizer),
                                candidate
                                        .predictionParticipants()
                                        .normalizedKey(normalizer),
                                null
                        )
                );
            }

            runRepository.complete(
                    runId,
                    clock.instant(),
                    candidates.size(),
                    autoApproved,
                    reviewRequired
            );

            return new AutomatedMatchingResult(
                    runId,
                    candidates.size(),
                    autoApproved,
                    reviewRequired,
                    rejected,
                    superseded
            );
        } catch (RuntimeException exception) {
            runRepository.fail(
                    runId,
                    clock.instant(),
                    exception.getMessage()
            );

            throw exception;
        }
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value);
    }
}