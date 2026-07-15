package com.ufcstudy.odds.http;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record HttpTransportResponse(
        int statusCode,
        String body,
        Map<String, List<String>> headers
) {

    public HttpTransportResponse {
        Objects.requireNonNull(body);
        Objects.requireNonNull(headers);

        headers = Map.copyOf(headers);
    }

    public Optional<String> firstHeader(String name) {
        return headers.entrySet()
                .stream()
                .filter(entry -> entry.getKey() != null)
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .filter(values -> !values.isEmpty())
                .map(List::getFirst)
                .findFirst();
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}