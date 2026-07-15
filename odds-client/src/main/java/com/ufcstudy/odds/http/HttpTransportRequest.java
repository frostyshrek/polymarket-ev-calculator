package com.ufcstudy.odds.http;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public record HttpTransportRequest(
        URI uri,
        Map<String, String> headers,
        Duration timeout
) {

    public HttpTransportRequest {
        Objects.requireNonNull(uri);
        Objects.requireNonNull(headers);
        Objects.requireNonNull(timeout);

        headers = Map.copyOf(headers);
    }
}