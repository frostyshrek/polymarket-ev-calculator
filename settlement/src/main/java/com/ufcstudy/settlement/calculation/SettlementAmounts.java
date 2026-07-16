package com.ufcstudy.settlement.calculation;

import com.ufcstudy.domain.paperbetting.PaperBetResult;

import java.math.BigDecimal;
import java.util.Objects;

public record SettlementAmounts(
        PaperBetResult result,
        BigDecimal grossReturnUnits,
        BigDecimal netProfitUnits
) {

    public SettlementAmounts {
        Objects.requireNonNull(result);
        Objects.requireNonNull(grossReturnUnits);
        Objects.requireNonNull(netProfitUnits);
    }
}