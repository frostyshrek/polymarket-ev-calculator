package com.ufcstudy.console.settlement;

import java.time.Instant;
import java.util.UUID;

public record SettleableEvent(
        UUID sportingEventId,
        String eventName,
        Instant scheduledStartTime,
        String officialResultType,
        String officialResultText,
        int openPaperBetCount
) {
}