package com.ufcstudy.console.operation;

import org.springframework.boot.context.properties
        .ConfigurationProperties;

@ConfigurationProperties(
        prefix = "ufc-study.sportsbook"
)
public record SportsbookOperationProperties(
        boolean enabled,
        String apiKey,
        String baseUrl,
        String region,
        String market
) {

    public void validateForExecution() {
        if (!enabled) {
            throw new IllegalStateException(
                    "Sportsbook ingestion is disabled. "
                            + "Set SPORTSBOOK_INGESTION_ENABLED=true."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "SPORTSBOOK_API_KEY is not configured."
            );
        }

        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException(
                    "SPORTSBOOK_API_BASE_URL is not configured."
            );
        }
    }
}