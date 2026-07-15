package com.ufcstudy.polymarket.http;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public record PolymarketHttpRequest(
        URI uri,
        Map<String, String> headers,
        Duration timeout
) {

    public PolymarketHttpRequest {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(headers);
        Objects.requireNonNull(timeout);

        headers = Map.copyOf(headers);
    }
}