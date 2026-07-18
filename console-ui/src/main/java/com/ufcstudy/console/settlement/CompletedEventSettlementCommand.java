package com.ufcstudy.console.settlement;

import java.util.List;

public interface CompletedEventSettlementCommand {

    List<SettleableEvent> findEligibleEvents();

    CompletedEventSettlementResult settleAll(
            String resolvedBy
    );
}