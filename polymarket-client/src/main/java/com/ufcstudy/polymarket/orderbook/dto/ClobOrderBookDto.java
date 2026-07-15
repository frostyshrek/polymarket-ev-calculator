package com.ufcstudy.polymarket.orderbook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClobOrderBookDto(
        String market,

        @JsonProperty("asset_id")
        String assetId,

        String timestamp,
        String hash,

        List<ClobPriceLevelDto> bids,
        List<ClobPriceLevelDto> asks,

        @JsonProperty("min_order_size")
        BigDecimal minimumOrderSize,

        @JsonProperty("tick_size")
        BigDecimal tickSize,

        @JsonProperty("neg_risk")
        boolean negativeRisk,

        @JsonProperty("last_trade_price")
        BigDecimal lastTradePrice
) {
}