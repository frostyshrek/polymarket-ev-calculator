package com.ufcstudy.eventmatching.automated;

import org.junit.jupiter.api.Test;

import com.ufcstudy.eventmatching.automated.FighterNameNormalizer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FighterNameNormalizerTest {

    private final FighterNameNormalizer normalizer =
            new FighterNameNormalizer();

    @Test
    void normalizesAccentsPunctuationAndSuffixes() {
        assertEquals(
                "jose aldo",
                normalizer.normalize("José Aldo")
        );

        assertEquals(
                "sean o malley",
                normalizer.normalize("Sean O'Malley")
        );

        assertEquals(
                "jon jones",
                normalizer.normalize("Jon Jones Jr.")
        );
    }
}