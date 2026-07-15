package com.ufcstudy.polymarket.orderbook.model;

import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.util.Objects;

public record OrderBookLevel(
        Probability price,
        BigDecimal size
) {

    public OrderBookLevel {
        Objects.requireNonNull(price);
        Objects.requireNonNull(size);

        if (size.signum() < 0) {
            throw new IllegalArgumentException(
                    "Order-book size cannot be negative"
            );
        }
    }

    public BigDecimal notional() {
        return price.value().multiply(size);
    }
}