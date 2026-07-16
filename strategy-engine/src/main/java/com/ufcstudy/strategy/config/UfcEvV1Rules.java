package com.ufcstudy.strategy.config;

import com.ufcstudy.domain.strategy.ProbabilityMethod;
import com.ufcstudy.domain.strategy.StrategyVersionIds;
import com.ufcstudy.strategy.model.StrategyRules;

import java.math.BigDecimal;
import java.time.Duration;

public final class UfcEvV1Rules {

    private UfcEvV1Rules() {
    }

    public static StrategyRules create() {
        return new StrategyRules(
                StrategyVersionIds.UFC_EV_V1,
                "calculation-engine-v1",
                ProbabilityMethod.BEST_BID,
                new BigDecimal("0.05"),
                new BigDecimal("0.04"),
                Duration.ofSeconds(60),
                Duration.ofSeconds(60),
                Duration.ofMinutes(30)
        );
    }
}