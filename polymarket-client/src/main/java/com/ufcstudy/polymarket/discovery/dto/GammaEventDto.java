package com.ufcstudy.polymarket.discovery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GammaEventDto(
        String id,
        String ticker,
        String slug,
        String title,
        String description,

        @JsonProperty("startDate")
        Instant startDate,

        @JsonProperty("endDate")
        Instant endDate,

        boolean active,
        boolean closed,
        boolean archived,

        @JsonProperty("liquidity")
        String liquidity,

        @JsonProperty("volume")
        String volume,

        List<GammaMarketDto> markets,
        List<GammaTagDto> tags
) {
}