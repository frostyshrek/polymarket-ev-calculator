package com.ufcstudy.polymarket.discovery.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GammaTagDto(
        String id,
        String label,
        String slug
) {
}