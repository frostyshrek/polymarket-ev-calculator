package com.ufcstudy.odds.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcstudy.odds.dto.OddsApiEventDto;
import com.ufcstudy.odds.exception.SportsbookAuthenticationException;
import com.ufcstudy.odds.exception.SportsbookClientException;
import com.ufcstudy.odds.exception.SportsbookQuotaException;
import com.ufcstudy.odds.exception.SportsbookRateLimitException;
import com.ufcstudy.odds.exception.SportsbookResponseException;
import com.ufcstudy.odds.http.HttpTransport;
import com.ufcstudy.odds.http.HttpTransportRequest;
import com.ufcstudy.odds.http.HttpTransportResponse;
import com.ufcstudy.odds.model.ProviderQuota;
import com.ufcstudy.odds.model.SportsbookOddsBatch;
import com.ufcstudy.odds.model.SportsbookOddsRequest;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TheOddsApiClient implements SportsbookOddsClient {

    public static final String PROVIDER_CODE = "THE_ODDS_API";

    private static final TypeReference<List<OddsApiEventDto>>
            EVENT_LIST_TYPE = new TypeReference<>() {
            };

    private final HttpTransport transport;
    private final ObjectMapper objectMapper;
    private final OddsApiUriFactory uriFactory;
    private final OddsApiMapper mapper;
    private final OddsApiQuotaParser quotaParser;
    private final Duration requestTimeout;
    private final Clock clock;

    public TheOddsApiClient(
            HttpTransport transport,
            ObjectMapper objectMapper,
            OddsApiUriFactory uriFactory,
            OddsApiMapper mapper,
            OddsApiQuotaParser quotaParser,
            Duration requestTimeout,
            Clock clock
    ) {
        this.transport = Objects.requireNonNull(transport);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.uriFactory = Objects.requireNonNull(uriFactory);
        this.mapper = Objects.requireNonNull(mapper);
        this.quotaParser = Objects.requireNonNull(quotaParser);
        this.requestTimeout = Objects.requireNonNull(requestTimeout);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public String providerCode() {
        return PROVIDER_CODE;
    }

    @Override
    public SportsbookOddsBatch fetchCurrentOdds(
            SportsbookOddsRequest request
    ) {
        HttpTransportRequest transportRequest =
                new HttpTransportRequest(
                        uriFactory.currentOddsUri(request),
                        Map.of(
                                "Accept", "application/json",
                                "User-Agent",
                                "ufc-market-value-study/0.1"
                        ),
                        requestTimeout
                );

        try {
            HttpTransportResponse response =
                    transport.get(transportRequest);

            ProviderQuota quota = quotaParser.parse(response);

            validateResponse(response);

            List<OddsApiEventDto> dtoEvents =
                    objectMapper.readValue(
                            response.body(),
                            EVENT_LIST_TYPE
                    );

            Instant receivedAt = clock.instant();

            return new SportsbookOddsBatch(
                    PROVIDER_CODE,
                    receivedAt,
                    mapper.map(dtoEvents),
                    quota,
                    response.body()
            );
        } catch (JsonProcessingException exception) {
            throw new SportsbookClientException(
                    "The sportsbook response was not valid JSON",
                    exception
            );
        } catch (IOException exception) {
            throw new SportsbookClientException(
                    "I/O failure while requesting sportsbook odds",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new SportsbookClientException(
                    "Sportsbook request was interrupted",
                    exception
            );
        }
    }

    private static void validateResponse(
            HttpTransportResponse response
    ) {
        switch (response.statusCode()) {
            case 200 -> {
                return;
            }
            case 401, 403 -> throw new SportsbookAuthenticationException(
                    "The sportsbook provider rejected the credentials"
            );
            case 429 -> throw new SportsbookRateLimitException(
                    "The sportsbook provider rate-limited the request",
                    Duration.ofSeconds(3)
            );
            default -> {
                if (response.body().contains("OUT_OF_USAGE_CREDITS")) {
                    throw new SportsbookQuotaException(
                            "The sportsbook provider usage quota is exhausted"
                    );
                }

                throw new SportsbookResponseException(
                        response.statusCode(),
                        "Sportsbook provider returned HTTP "
                                + response.statusCode()
                );
            }
        }
    }
}