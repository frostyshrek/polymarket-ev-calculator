package com.ufcstudy.odds.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OddsApiMarketDto(
        String key,

        @JsonProperty("last_update")
        Instant lastUpdate,

        List<OddsApiOutcomeDto> outcomes
) {
}