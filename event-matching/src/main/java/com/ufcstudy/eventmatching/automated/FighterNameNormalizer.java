package com.ufcstudy.eventmatching.automated;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

public final class FighterNameNormalizer {

    public String normalize(String value) {
        Objects.requireNonNull(value);

        String normalized = Normalizer.normalize(
                value,
                Normalizer.Form.NFD
        );

        normalized = normalized.replaceAll(
                "\\p{M}+",
                ""
        );

        normalized = normalized
                .toLowerCase(Locale.ROOT)
                .replace('&', ' ')
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        normalized = removeSuffix(normalized, "jr");
        normalized = removeSuffix(normalized, "sr");
        normalized = removeSuffix(normalized, "ii");
        normalized = removeSuffix(normalized, "iii");
        normalized = removeSuffix(normalized, "iv");

        return normalized;
    }

    private String removeSuffix(
            String value,
            String suffix
    ) {
        String ending = " " + suffix;

        if (value.endsWith(ending)) {
            return value.substring(
                    0,
                    value.length() - ending.length()
            ).trim();
        }

        return value;
    }
}