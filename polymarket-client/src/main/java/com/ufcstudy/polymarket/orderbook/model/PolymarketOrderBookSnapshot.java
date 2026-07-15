package com.ufcstudy.polymarket.orderbook.model;

import com.ufcstudy.domain.value.Probability;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record PolymarketOrderBookSnapshot(
        String conditionId,
        String tokenId,
        Instant sourceTimestamp,
        Instant receivedAt,
        String orderBookHash,
        List<OrderBookLevel> bids,
        List<OrderBookLevel> asks,
        Probability bestBid,
        Probability bestAsk,
        Probability midpoint,
        BigDecimal spread,
        BigDecimal bidDepth,
        BigDecimal askDepth,
        BigDecimal minimumOrderSize,
        BigDecimal tickSize,
        Probability lastTradePrice,
        boolean negativeRisk,
        String rawPayload
) {

    public PolymarketOrderBookSnapshot {
        Objects.requireNonNull(tokenId);
        Objects.requireNonNull(receivedAt);
        Objects.requireNonNull(bids);
        Objects.requireNonNull(asks);
        Objects.requireNonNull(rawPayload);

        bids = List.copyOf(bids);
        asks = List.copyOf(asks);
    }

    public boolean hasTwoSidedBook() {
        return bestBid != null && bestAsk != null;
    }
}