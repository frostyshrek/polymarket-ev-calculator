package com.ufcstudy.persistence.market.repository;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public final class DeterministicUuidFactory {

    public UUID from(String namespace, String externalKey) {
        Objects.requireNonNull(namespace);
        Objects.requireNonNull(externalKey);

        String input = namespace + ":" + externalKey;

        return UUID.nameUUIDFromBytes(
                input.getBytes(StandardCharsets.UTF_8)
        );
    }
}