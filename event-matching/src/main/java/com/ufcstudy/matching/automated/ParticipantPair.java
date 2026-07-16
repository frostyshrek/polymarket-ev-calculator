package com.ufcstudy.eventmatching.automated;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public record ParticipantPair(
        List<String> participantNames
) {

    public ParticipantPair {
        Objects.requireNonNull(participantNames);

        if (participantNames.size() != 2) {
            throw new IllegalArgumentException(
                    "UFC winner markets must contain exactly two participants"
            );
        }

        participantNames = List.copyOf(participantNames);
    }

    public String normalizedKey(
            FighterNameNormalizer normalizer
    ) {
        return participantNames.stream()
                .map(normalizer::normalize)
                .sorted()
                .collect(Collectors.joining("|"));
    }
}