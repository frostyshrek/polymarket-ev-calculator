package com.ufcstudy.polymarket.orderbook.provider;

import com.ufcstudy.calculation.MarketQuoteCalculator;
import com.ufcstudy.polymarket.config.PolymarketObjectMapperFactory;
import com.ufcstudy.polymarket.config.PolymarketProperties;
import com.ufcstudy.polymarket.http.FakePolymarketHttpTransport;
import com.ufcstudy.polymarket.http.PolymarketHttpResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClobPolymarketClientTest {

    @Test
    void mapsOrderBookAndCalculatesQuote()
            throws IOException {

        String body = readFixture(
                "/fixtures/polymarket-order-book.json"
        );

        FakePolymarketHttpTransport transport =
                new FakePolymarketHttpTransport();

        transport.enqueue(
                new PolymarketHttpResponse(
                        200,
                        body,
                        Map.of()
                )
        );

        PolymarketProperties properties = testProperties();

        ClobPolymarketClient client =
                new ClobPolymarketClient(
                        transport,
                        PolymarketObjectMapperFactory.create(),
                        new ClobUriFactory(properties),
                        new ClobOrderBookMapper(
                                new MarketQuoteCalculator(),
                                2
                        ),
                        properties.requestTimeout(),
                        Clock.fixed(
                                Instant.parse(
                                        "2026-07-15T10:00:00Z"
                                ),
                                ZoneOffset.UTC
                        )
                );

        var snapshot = client.fetchOrderBook("token-a");

        assertEquals("token-a", snapshot.tokenId());
        assertTrue(snapshot.hasTwoSidedBook());

        assertEquals(
                "0.58",
                snapshot.bestBid().value().toPlainString()
        );

        assertEquals(
                "0.61",
                snapshot.bestAsk().value().toPlainString()
        );

        assertEquals(
                "0.595",
                snapshot.midpoint().value().toPlainString()
        );

        assertEquals(
                "0.03",
                snapshot.spread().toPlainString()
        );

        /*
         * Best two bid levels: 100 + 200.
         */
        assertEquals(
                "300",
                snapshot.bidDepth().toPlainString()
        );

        /*
         * Best two ask levels: 150 + 250.
         */
        assertEquals(
                "400",
                snapshot.askDepth().toPlainString()
        );

        assertEquals(
                "0.59",
                snapshot.lastTradePrice()
                        .value()
                        .toPlainString()
        );

        assertEquals(1, transport.requests().size());
    }

    private static PolymarketProperties testProperties() {
        return new PolymarketProperties(
                true,
                URI.create(
                        "https://gamma-api.polymarket.com"
                ),
                URI.create(
                        "https://clob.polymarket.com"
                ),
                Duration.ofSeconds(5),
                Duration.ofSeconds(15),
                Duration.ofHours(1),
                Duration.ofMinutes(5),
                100,
                20,
                2,
                5
        );
    }

    private static String readFixture(String resource)
            throws IOException {

        try (var input =
                     ClobPolymarketClientTest.class
                             .getResourceAsStream(resource)) {

            if (input == null) {
                throw new IOException(
                        "Fixture not found: " + resource
                );
            }

            return new String(
                    input.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }
}