package com.ufcstudy.eventmatching.model;

import com.ufcstudy.domain.matching.MappingStatus;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record ManualEventMatchResult(
        UUID sportingEventId,
        List<UUID> participantIds,
        List<UUID> mappingIds,
        MappingStatus status
) {

    public ManualEventMatchResult {
        Objects.requireNonNull(sportingEventId);
        Objects.requireNonNull(participantIds);
        Objects.requireNonNull(mappingIds);
        Objects.requireNonNull(status);

        participantIds = List.copyOf(participantIds);
        mappingIds = List.copyOf(mappingIds);
    }
}