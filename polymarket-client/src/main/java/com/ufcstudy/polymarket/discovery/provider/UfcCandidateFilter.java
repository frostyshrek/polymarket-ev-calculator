package com.ufcstudy.polymarket.discovery.provider;

import com.ufcstudy.polymarket.discovery.model.PolymarketEvent;

import java.util.Locale;
import java.util.Set;

public final class UfcCandidateFilter {

    private static final Set<String> RELEVANT_TAGS = Set.of(
            "ufc",
            "mma",
            "mixed-martial-arts"
    );

    public boolean isCandidate(PolymarketEvent event) {
        if (event == null || !event.active() || event.closed()) {
            return false;
        }

        boolean matchingTag = event.tags()
                .stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(RELEVANT_TAGS::contains);

        String searchableText = (
                nullToEmpty(event.title())
                        + " "
                        + nullToEmpty(event.description())
        ).toLowerCase(Locale.ROOT);

        boolean matchingText =
                searchableText.contains("ufc")
                        || searchableText.contains("mma")
                        || searchableText.contains(
                                "mixed martial arts"
                        );

        return matchingTag || matchingText;
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}