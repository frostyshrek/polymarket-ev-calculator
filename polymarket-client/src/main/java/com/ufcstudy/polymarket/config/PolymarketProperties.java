package com.ufcstudy.polymarket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.Objects;

@ConfigurationProperties(prefix = "providers.polymarket")
public record PolymarketProperties(
        boolean enabled,
        URI gammaBaseUrl,
        URI clobBaseUrl,
        Duration connectTimeout,
        Duration requestTimeout,
        Duration discoveryPollingInterval,
        Duration orderBookPollingInterval,
        int pageSize,
        int maximumPages,
        int maximumRetries,
        int depthLevels
) {

    public PolymarketProperties {
        Objects.requireNonNull(gammaBaseUrl);
        Objects.requireNonNull(clobBaseUrl);
        Objects.requireNonNull(connectTimeout);
        Objects.requireNonNull(requestTimeout);
        Objects.requireNonNull(discoveryPollingInterval);
        Objects.requireNonNull(orderBookPollingInterval);

        requirePositive(connectTimeout, "Connect timeout");
        requirePositive(requestTimeout, "Request timeout");
        requirePositive(
                discoveryPollingInterval,
                "Discovery polling interval"
        );
        requirePositive(
                orderBookPollingInterval,
                "Order-book polling interval"
        );

        if (pageSize < 1 || pageSize > 500) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 500"
            );
        }

        if (maximumPages < 1) {
            throw new IllegalArgumentException(
                    "Maximum pages must be positive"
            );
        }

        if (maximumRetries < 0 || maximumRetries > 5) {
            throw new IllegalArgumentException(
                    "Maximum retries must be between 0 and 5"
            );
        }

        if (depthLevels < 1) {
            throw new IllegalArgumentException(
                    "Depth levels must be positive"
            );
        }
    }

    private static void requirePositive(
            Duration value,
            String fieldName
    ) {
        if (value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }
    }
}