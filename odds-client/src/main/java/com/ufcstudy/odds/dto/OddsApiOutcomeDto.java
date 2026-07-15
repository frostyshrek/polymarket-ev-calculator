package com.ufcstudy.odds.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OddsApiOutcomeDto(
        String name,
        BigDecimal price
) {
}