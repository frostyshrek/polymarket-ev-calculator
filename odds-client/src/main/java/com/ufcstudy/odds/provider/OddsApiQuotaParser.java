package com.ufcstudy.odds.provider;

import com.ufcstudy.odds.http.HttpTransportResponse;
import com.ufcstudy.odds.model.ProviderQuota;

public final class OddsApiQuotaParser {

    public ProviderQuota parse(HttpTransportResponse response) {
        return new ProviderQuota(
                parseLong(response, "x-requests-remaining"),
                parseLong(response, "x-requests-used"),
                parseLong(response, "x-requests-last")
        );
    }

    private static Long parseLong(
            HttpTransportResponse response,
            String header
    ) {
        return response.firstHeader(header)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> {
                    try {
                        return Long.valueOf(value);
                    } catch (NumberFormatException exception) {
                        return null;
                    }
                })
                .orElse(null);
    }
}