package com.ufcstudy.odds.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

@ConfigurationProperties(prefix = "providers.the-odds-api")
public record OddsApiProperties(
        boolean enabled,
        URI baseUrl,
        String apiKey,
        String sportKey,
        List<String> regions,
        List<String> markets,
        String oddsFormat,
        String dateFormat,
        Duration connectTimeout,
        Duration requestTimeout,
        Duration pollingInterval,
        int lowQuotaThreshold,
        int maximumRetries
) {

    public OddsApiProperties {
        Objects.requireNonNull(baseUrl, "Base URL cannot be null");
        Objects.requireNonNull(apiKey, "API key cannot be null");
        Objects.requireNonNull(sportKey, "Sport key cannot be null");
        Objects.requireNonNull(regions, "Regions cannot be null");
        Objects.requireNonNull(markets, "Markets cannot be null");
        Objects.requireNonNull(oddsFormat, "Odds format cannot be null");
        Objects.requireNonNull(dateFormat, "Date format cannot be null");
        Objects.requireNonNull(connectTimeout);
        Objects.requireNonNull(requestTimeout);
        Objects.requireNonNull(pollingInterval);

        if (enabled && apiKey.isBlank()) {
            throw new IllegalArgumentException(
                    "The Odds API key is required when the provider is enabled"
            );
        }

        if (regions.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one bookmaker region is required"
            );
        }

        if (markets.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one market is required"
            );
        }

        if (connectTimeout.isNegative() || connectTimeout.isZero()) {
            throw new IllegalArgumentException(
                    "Connect timeout must be positive"
            );
        }

        if (requestTimeout.isNegative() || requestTimeout.isZero()) {
            throw new IllegalArgumentException(
                    "Request timeout must be positive"
            );
        }

        if (pollingInterval.isNegative() || pollingInterval.isZero()) {
            throw new IllegalArgumentException(
                    "Polling interval must be positive"
            );
        }

        if (lowQuotaThreshold < 0) {
            throw new IllegalArgumentException(
                    "Low quota threshold cannot be negative"
            );
        }

        if (maximumRetries < 0 || maximumRetries > 5) {
            throw new IllegalArgumentException(
                    "Maximum retries must be between 0 and 5"
            );
        }
    }
}