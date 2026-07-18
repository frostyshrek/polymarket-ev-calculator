package com.ufcstudy.console.operation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ufc-study.polymarket")
public record PolymarketOperationProperties(
        boolean enabled,
        String gammaBaseUrl,
        String clobBaseUrl,
        String eventSlug
) {

    public void validateForExecution() {
        if (!enabled) {
            throw new IllegalStateException(
                    "Polymarket ingestion is disabled. "
                            + "Set POLYMARKET_INGESTION_ENABLED=true."
            );
        }

        requireValue(
                gammaBaseUrl,
                "POLYMARKET_GAMMA_BASE_URL"
        );

        requireValue(
                clobBaseUrl,
                "POLYMARKET_CLOB_BASE_URL"
        );

        requireValue(
                eventSlug,
                "POLYMARKET_EVENT_SLUG"
        );
    }

    private static void requireValue(
            String value,
            String settingName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(
                    settingName + " is not configured."
            );
        }
    }
}