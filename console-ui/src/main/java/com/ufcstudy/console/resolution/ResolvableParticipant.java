package com.ufcstudy.console.resolution;

import java.util.UUID;

public record ResolvableParticipant(
        UUID participantId,
        String participantName,
        int displayOrder
) {
}