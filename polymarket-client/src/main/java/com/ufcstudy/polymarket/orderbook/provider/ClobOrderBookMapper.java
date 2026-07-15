package com.ufcstudy.polymarket.orderbook.provider;

import com.ufcstudy.calculation.MarketQuoteCalculator;
import com.ufcstudy.calculation.MarketQuoteResult;
import com.ufcstudy.domain.value.Probability;
import com.ufcstudy.polymarket.orderbook.dto.ClobOrderBookDto;
import com.ufcstudy.polymarket.orderbook.dto.ClobPriceLevelDto;
import com.ufcstudy.polymarket.orderbook.model.OrderBookLevel;
import com.ufcstudy.polymarket.orderbook.model.PolymarketOrderBookSnapshot;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class ClobOrderBookMapper {

    private final MarketQuoteCalculator quoteCalculator;
    private final int depthLevels;

    public ClobOrderBookMapper(
            MarketQuoteCalculator quoteCalculator,
            int depthLevels
    ) {
        this.quoteCalculator =
                Objects.requireNonNull(quoteCalculator);

        if (depthLevels < 1) {
            throw new IllegalArgumentException(
                    "Depth levels must be positive"
            );
        }

        this.depthLevels = depthLevels;
    }

    public PolymarketOrderBookSnapshot map(
            ClobOrderBookDto source,
            Instant receivedAt,
            String rawPayload
    ) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(receivedAt);
        Objects.requireNonNull(rawPayload);

        List<OrderBookLevel> bids = safeList(source.bids())
                .stream()
                .map(this::mapLevel)
                .sorted(
                        Comparator.comparing(
                                (OrderBookLevel level) ->
                                        level.price().value()
                        ).reversed()
                )
                .toList();

        List<OrderBookLevel> asks = safeList(source.asks())
                .stream()
                .map(this::mapLevel)
                .sorted(
                        Comparator.comparing(
                                level -> level.price().value()
                        )
                )
                .toList();

        Probability bestBid = bids.isEmpty()
                ? null
                : bids.getFirst().price();

        Probability bestAsk = asks.isEmpty()
                ? null
                : asks.getFirst().price();

        Probability midpoint = null;
        BigDecimal spread = null;

        if (bestBid != null && bestAsk != null) {
            MarketQuoteResult quote = quoteCalculator.calculate(
                    bestBid,
                    bestAsk
            );

            midpoint = quote.midpoint();
            spread = quote.spread();
        }

        return new PolymarketOrderBookSnapshot(
                source.market(),
                source.assetId(),
                parseTimestamp(source.timestamp()),
                receivedAt,
                source.hash(),
                bids,
                asks,
                bestBid,
                bestAsk,
                midpoint,
                spread,
                calculateDepth(bids),
                calculateDepth(asks),
                source.minimumOrderSize(),
                source.tickSize(),
                nullableProbability(source.lastTradePrice()),
                source.negativeRisk(),
                rawPayload
        );
    }

    private OrderBookLevel mapLevel(ClobPriceLevelDto source) {
        if (source.price() == null || source.size() == null) {
            throw new IllegalArgumentException(
                    "Order-book level price and size are required"
            );
        }

        return new OrderBookLevel(
                Probability.of(source.price()),
                source.size()
        );
    }

    private BigDecimal calculateDepth(
            List<OrderBookLevel> levels
    ) {
        return levels.stream()
                .limit(depthLevels)
                .map(OrderBookLevel::size)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static Probability nullableProbability(
            BigDecimal value
    ) {
        return value == null ? null : Probability.of(value);
    }

    private static Instant parseTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        long rawTimestamp = Long.parseLong(value);

        /*
         * Detect milliseconds by magnitude.
         */
        return rawTimestamp > 10_000_000_000L
                ? Instant.ofEpochMilli(rawTimestamp)
                : Instant.ofEpochSecond(rawTimestamp);
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}