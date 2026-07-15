package com.ufcstudy.polymarket.http;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record PolymarketHttpResponse(
        int statusCode,
        String body,
        Map<String, List<String>> headers
) {

    public PolymarketHttpResponse {
        Objects.requireNonNull(body);
        Objects.requireNonNull(headers);

        headers = Map.copyOf(headers);
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}