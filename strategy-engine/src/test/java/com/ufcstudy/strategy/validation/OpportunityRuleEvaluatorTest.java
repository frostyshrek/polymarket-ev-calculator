package com.ufcstudy.strategy.validation;

import com.ufcstudy.persistence.opportunity.model.OpportunityCandidateRecord;
import com.ufcstudy.strategy.calculation.OpportunityTiming;
import com.ufcstudy.strategy.config.UfcEvV1Rules;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpportunityRuleEvaluatorTest {

    @Test
    void qualifiesWhenAllRulesPass() {
        OpportunityCandidateRecord candidate =
                candidate(
                        new BigDecimal("0.03")
                );

        OpportunityTiming timing =
                new OpportunityTiming(
                        20,
                        15,
                        5,
                        7200
                );

        var failures = new OpportunityRuleEvaluator()
                .evaluate(
                        candidate,
                        UfcEvV1Rules.create(),
                        timing,
                        new BigDecimal("0.58"),
                        new BigDecimal("0.102")
                );

        assertTrue(failures.isEmpty());
    }

    private static OpportunityCandidateRecord candidate(
            BigDecimal spread
    ) {
        Instant now = Instant.parse(
                "2026-08-01T10:00:00Z"
        );

        return new OpportunityCandidateRecord(
                UUID.randomUUID(),
                UUID.randomUUID(),
                now.plusSeconds(7200),
                "APPROVED_MANUAL",
                "EXACT",
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "test_book",
                new BigDecimal("1.90"),
                new BigDecimal("2.00"),
                now.minusSeconds(20),
                now.minusSeconds(20),
                now.minusSeconds(15),
                new BigDecimal("0.58"),
                new BigDecimal("0.61"),
                new BigDecimal("0.595"),
                spread
        );
    }
}