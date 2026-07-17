package com.ufcstudy.eventmatching.model;

import java.util.Objects;

public record FighterSpecification(
        String canonicalName,
        String sportsbookName,
        String polymarketName
) {

    public FighterSpecification {
        requireText(canonicalName, "Canonical name");
        requireText(sportsbookName, "Sportsbook name");
        requireText(polymarketName, "Polymarket name");
    }

    private static void requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(value, fieldName + " cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }
    }
}