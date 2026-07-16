package com.ufcstudy.strategy.calculation;

import com.ufcstudy.domain.strategy.ProbabilityMethod;
import com.ufcstudy.persistence.opportunity.model.OpportunityCandidateRecord;

import java.math.BigDecimal;
import java.util.Objects;

public final class ReferenceProbabilitySelector {

    public BigDecimal select(
            ProbabilityMethod method,
            OpportunityCandidateRecord candidate
    ) {
        Objects.requireNonNull(method);
        Objects.requireNonNull(candidate);

        return switch (method) {
            case BEST_BID -> candidate.predictionBestBid();
            case MIDPOINT -> candidate.predictionMidpoint();

            case LAST_TRADE,
                 CALIBRATED,
                 ENSEMBLE ->
                    throw new UnsupportedOperationException(
                            "Probability method is not implemented: "
                                    + method
                    );
        };
    }
}