package com.ufcstudy.polymarket.orderbook.provider;

import com.ufcstudy.polymarket.config.PolymarketProperties;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ClobUriFactory {

    private final PolymarketProperties properties;

    public ClobUriFactory(PolymarketProperties properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    public URI orderBookUri(String tokenId) {
        requireTokenId(tokenId);

        return URI.create(
                normalizedBaseUrl()
                        + "/book?token_id="
                        + encode(tokenId)
        );
    }

    public URI midpointUri(String tokenId) {
        requireTokenId(tokenId);

        return URI.create(
                normalizedBaseUrl()
                        + "/midpoint?token_id="
                        + encode(tokenId)
        );
    }

    public URI spreadUri(String tokenId) {
        requireTokenId(tokenId);

        return URI.create(
                normalizedBaseUrl()
                        + "/spread?token_id="
                        + encode(tokenId)
        );
    }

    public URI lastTradePriceUri(String tokenId) {
        requireTokenId(tokenId);

        return URI.create(
                normalizedBaseUrl()
                        + "/last-trade-price?token_id="
                        + encode(tokenId)
        );
    }

    private String normalizedBaseUrl() {
        return properties.clobBaseUrl()
                .toString()
                .replaceAll("/+$", "");
    }

    private static void requireTokenId(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            throw new IllegalArgumentException(
                    "Token ID cannot be blank"
            );
        }
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}