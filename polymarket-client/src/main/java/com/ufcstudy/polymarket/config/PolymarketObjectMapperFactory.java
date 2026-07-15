package com.ufcstudy.polymarket.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class PolymarketObjectMapperFactory {

    private PolymarketObjectMapperFactory() {
    }

    public static ObjectMapper create() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(
                        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                );
    }
}