package com.ufcstudy.polymarket.discovery.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcstudy.polymarket.exception.PolymarketClientException;

import java.util.List;
import java.util.Objects;

public final class GammaArrayParser {

    private static final TypeReference<List<String>> STRING_LIST =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper;

    public GammaArrayParser(ObjectMapper objectMapper) {
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    public List<String> parseStrings(
            String json,
            String fieldName
    ) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return List.copyOf(
                    objectMapper.readValue(json, STRING_LIST)
            );
        } catch (JsonProcessingException exception) {
            throw new PolymarketClientException(
                    "Invalid Gamma array field: " + fieldName,
                    exception
            );
        }
    }
}