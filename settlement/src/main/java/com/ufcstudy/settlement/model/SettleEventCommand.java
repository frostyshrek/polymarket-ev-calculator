package com.ufcstudy.settlement.model;

import com.ufcstudy.domain.settlement.OfficialResultType;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record SettleEventCommand(
        UUID sportingEventId,
        OfficialResultType officialResultType,
        UUID winningParticipantId,
        String officialResultText,
        UUID resultSourceId,
        String sourceExternalResultId,
        Instant officialResultAt,
        Instant observedAt,
        String resolvedBy
) {

    public SettleEventCommand {
        Objects.requireNonNull(
                sportingEventId,
                "Sporting event ID cannot be null"
        );

        Objects.requireNonNull(
                officialResultType,
                "Official result type cannot be null"
        );

        Objects.requireNonNull(
                officialResultText,
                "Official result text cannot be null"
        );

        Objects.requireNonNull(
                observedAt,
                "Observed time cannot be null"
        );

        Objects.requireNonNull(
                resolvedBy,
                "Resolved by cannot be null"
        );

        if (officialResultText.isBlank()) {
            throw new IllegalArgumentException(
                    "Official result text cannot be blank"
            );
        }

        if (resolvedBy.isBlank()) {
            throw new IllegalArgumentException(
                    "Resolved by cannot be blank"
            );
        }

        if (officialResultType
                == OfficialResultType.PARTICIPANT_WIN
                && winningParticipantId == null) {
            throw new IllegalArgumentException(
                    "A participant win requires a winner"
            );
        }

        if (officialResultType
                != OfficialResultType.PARTICIPANT_WIN
                && winningParticipantId != null) {
            throw new IllegalArgumentException(
                    "Non-win results cannot specify a winner"
            );
        }
    }
}