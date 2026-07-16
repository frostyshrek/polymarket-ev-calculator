package com.ufcstudy.persistence;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public final class JdbcTime {

    private JdbcTime() {
    }

    public static OffsetDateTime from(Instant instant) {
        return instant == null
                ? null
                : instant.atOffset(ZoneOffset.UTC);
    }
}