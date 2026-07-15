package com.ufcstudy.polymarket.discovery.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcstudy.polymarket.discovery.dto.GammaEventDto;
import com.ufcstudy.polymarket.discovery.model.PolymarketDiscoveryBatch;
import com.ufcstudy.polymarket.exception.PolymarketClientException;
import com.ufcstudy.polymarket.exception.PolymarketResponseException;
import com.ufcstudy.polymarket.http.PolymarketHttpRequest;
import com.ufcstudy.polymarket.http.PolymarketHttpResponse;
import com.ufcstudy.polymarket.http.PolymarketHttpTransport;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class GammaPolymarketClient
        implements PolymarketDiscoveryClient {

    private static final TypeReference<List<GammaEventDto>>
            EVENT_LIST_TYPE = new TypeReference<>() {
            };

    private final PolymarketHttpTransport transport;
    private final ObjectMapper objectMapper;
    private final GammaUriFactory uriFactory;
    private final GammaMapper mapper;
    private final Duration requestTimeout;
    private final Clock clock;

    public GammaPolymarketClient(
            PolymarketHttpTransport transport,
            ObjectMapper objectMapper,
            GammaUriFactory uriFactory,
            GammaMapper mapper,
            Duration requestTimeout,
            Clock clock
    ) {
        this.transport = Objects.requireNonNull(transport);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.uriFactory = Objects.requireNonNull(uriFactory);
        this.mapper = Objects.requireNonNull(mapper);
        this.requestTimeout = Objects.requireNonNull(requestTimeout);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public PolymarketDiscoveryBatch fetchActiveEvents(
            int limit,
            int offset,
            String tagId
    ) {
        PolymarketHttpRequest request =
                new PolymarketHttpRequest(
                        uriFactory.activeEventsUri(
                                limit,
                                offset,
                                tagId
                        ),
                        Map.of(
                                "Accept", "application/json",
                                "User-Agent",
                                "ufc-market-value-study/0.1"
                        ),
                        requestTimeout
                );

        try {
            PolymarketHttpResponse response =
                    transport.get(request);

            validateResponse(response);

            List<GammaEventDto> events =
                    objectMapper.readValue(
                            response.body(),
                            EVENT_LIST_TYPE
                    );

            return new PolymarketDiscoveryBatch(
                    clock.instant(),
                    mapper.map(events),
                    response.body(),
                    offset,
                    limit
            );
        } catch (JsonProcessingException exception) {
            throw new PolymarketClientException(
                    "Gamma returned invalid JSON",
                    exception
            );
        } catch (IOException exception) {
            throw new PolymarketClientException(
                    "I/O failure while requesting Gamma events",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new PolymarketClientException(
                    "Gamma request was interrupted",
                    exception
            );
        }
    }

    private static void validateResponse(
            PolymarketHttpResponse response
    ) {
        if (!response.isSuccessful()) {
            throw new PolymarketResponseException(
                    response.statusCode(),
                    "Gamma returned HTTP " + response.statusCode()
            );
        }
    }
}