package com.ufcstudy.polymarket.orderbook.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufcstudy.polymarket.exception.OrderBookUnavailableException;
import com.ufcstudy.polymarket.exception.PolymarketClientException;
import com.ufcstudy.polymarket.exception.PolymarketResponseException;
import com.ufcstudy.polymarket.http.PolymarketHttpRequest;
import com.ufcstudy.polymarket.http.PolymarketHttpResponse;
import com.ufcstudy.polymarket.http.PolymarketHttpTransport;
import com.ufcstudy.polymarket.orderbook.dto.ClobOrderBookDto;
import com.ufcstudy.polymarket.orderbook.model.PolymarketOrderBookSnapshot;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public final class ClobPolymarketClient
        implements PolymarketOrderBookClient {

    private final PolymarketHttpTransport transport;
    private final ObjectMapper objectMapper;
    private final ClobUriFactory uriFactory;
    private final ClobOrderBookMapper mapper;
    private final Duration requestTimeout;
    private final Clock clock;

    public ClobPolymarketClient(
            PolymarketHttpTransport transport,
            ObjectMapper objectMapper,
            ClobUriFactory uriFactory,
            ClobOrderBookMapper mapper,
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
    public PolymarketOrderBookSnapshot fetchOrderBook(
            String tokenId
    ) {
        PolymarketHttpRequest request =
                new PolymarketHttpRequest(
                        uriFactory.orderBookUri(tokenId),
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

            validateResponse(response, tokenId);

            ClobOrderBookDto dto = objectMapper.readValue(
                    response.body(),
                    ClobOrderBookDto.class
            );

            return mapper.map(
                    dto,
                    clock.instant(),
                    response.body()
            );
        } catch (JsonProcessingException exception) {
            throw new PolymarketClientException(
                    "CLOB returned invalid order-book JSON",
                    exception
            );
        } catch (IOException exception) {
            throw new PolymarketClientException(
                    "I/O failure while requesting CLOB order book",
                    exception
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();

            throw new PolymarketClientException(
                    "CLOB request was interrupted",
                    exception
            );
        }
    }

    private static void validateResponse(
            PolymarketHttpResponse response,
            String tokenId
    ) {
        if (response.statusCode() == 404) {
            throw new OrderBookUnavailableException(
                    tokenId,
                    "No order book exists for the requested token"
            );
        }

        if (response.statusCode() == 400) {
            throw new PolymarketResponseException(
                    400,
                    "Invalid CLOB token ID"
            );
        }

        if (!response.isSuccessful()) {
            throw new PolymarketResponseException(
                    response.statusCode(),
                    "CLOB returned HTTP " + response.statusCode()
            );
        }
    }
}