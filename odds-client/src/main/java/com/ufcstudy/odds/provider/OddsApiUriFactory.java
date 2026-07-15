package com.ufcstudy.odds.provider;

import com.ufcstudy.odds.config.OddsApiProperties;
import com.ufcstudy.odds.model.SportsbookOddsRequest;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.stream.Collectors;

public final class OddsApiUriFactory {

    private final OddsApiProperties properties;

    public OddsApiUriFactory(OddsApiProperties properties) {
        this.properties = Objects.requireNonNull(properties);
    }

    public URI currentOddsUri(SportsbookOddsRequest request) {
        Objects.requireNonNull(request);

        String regions = joinEncoded(request.regions());
        String markets = joinEncoded(request.markets());

        String path = "/v4/sports/"
                + encode(request.sportKey())
                + "/odds";

        String query = "apiKey=" + encode(properties.apiKey())
                + "&regions=" + regions
                + "&markets=" + markets
                + "&oddsFormat=" + encode(
                        request.oddsFormat().apiValue()
                )
                + "&dateFormat=" + encode(properties.dateFormat());

        String baseUrl = properties.baseUrl().toString()
                .replaceAll("/+$", "");

        return URI.create(baseUrl + path + "?" + query);
    }

    private static String joinEncoded(Iterable<String> values) {
        return java.util.stream.StreamSupport
                .stream(values.spliterator(), false)
                .map(OddsApiUriFactory::encode)
                .collect(Collectors.joining(","));
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}