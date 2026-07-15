package com.ufcstudy.polymarket.orderbook.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClobPriceLevelDto(
        BigDecimal price,
        BigDecimal size
) {
}