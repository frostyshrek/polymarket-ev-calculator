package com.ufcstudy.polymarket.discovery.provider;

import com.ufcstudy.polymarket.config.PolymarketProperties;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class GammaUriFactory {

    private final PolymarketProperties properties;

    public GammaUriFactory(PolymarketProperties properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    public URI activeEventsUri(
            int limit,
            int offset,
            String tagId
    ) {
        if (limit < 1) {
            throw new IllegalArgumentException(
                    "Limit must be positive"
            );
        }

        if (offset < 0) {
            throw new IllegalArgumentException(
                    "Offset cannot be negative"
            );
        }

        StringBuilder query = new StringBuilder()
                .append("active=true")
                .append("&closed=false")
                .append("&limit=").append(limit)
                .append("&offset=").append(offset)
                .append("&order=end_date")
                .append("&ascending=true");

        if (tagId != null && !tagId.isBlank()) {
            query.append("&tag_id=").append(encode(tagId));
        }

        return URI.create(
                normalizedBaseUrl()
                        + "/events?"
                        + query
        );
    }

    public URI sportsMetadataUri() {
        return URI.create(normalizedBaseUrl() + "/sports");
    }

    private String normalizedBaseUrl() {
        return properties.gammaBaseUrl()
                .toString()
                .replaceAll("/+$", "");
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}