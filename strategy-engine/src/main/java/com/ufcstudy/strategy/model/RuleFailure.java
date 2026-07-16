package com.ufcstudy.strategy.model;

import com.ufcstudy.domain.strategy.OpportunityRejectionCode;

import java.util.Objects;

public record RuleFailure(
        OpportunityRejectionCode code,
        String message
) {

    public RuleFailure {
        Objects.requireNonNull(code);
        Objects.requireNonNull(message);

        if (message.isBlank()) {
            throw new IllegalArgumentException(
                    "Rule failure message cannot be blank"
            );
        }
    }
}