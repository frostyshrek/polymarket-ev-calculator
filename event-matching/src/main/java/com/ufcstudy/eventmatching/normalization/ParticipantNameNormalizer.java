package com.ufcstudy.eventmatching.normalization;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;

public final class ParticipantNameNormalizer {

    public String normalize(String value) {
        Objects.requireNonNull(value, "Name cannot be null");

        String decomposed = Normalizer.normalize(
                value,
                Normalizer.Form.NFKD
        );

        return decomposed
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[\"'’`]", "")
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}