package com.ufcstudy.polymarket.discovery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GammaMarketDto(
        String id,

        @JsonProperty("question")
        String question,

        @JsonProperty("conditionId")
        String conditionId,

        String slug,

        @JsonProperty("resolutionSource")
        String resolutionSource,

        @JsonProperty("endDate")
        Instant endDate,

        boolean active,
        boolean closed,
        boolean archived,

        @JsonProperty("enableOrderBook")
        boolean enableOrderBook,

        String outcomes,

        @JsonProperty("outcomePrices")
        String outcomePrices,

        @JsonProperty("clobTokenIds")
        String clobTokenIds,

        String liquidity,
        String volume,

        @JsonProperty("minimumTickSize")
        String minimumTickSize
) {
}