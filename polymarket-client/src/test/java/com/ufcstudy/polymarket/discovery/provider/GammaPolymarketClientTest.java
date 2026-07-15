package com.ufcstudy.polymarket.discovery.provider;

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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GammaPolymarketClientTest {

    @Test
    void discoversAndMapsActiveUfcEvent() throws IOException {
        String body = readFixture(
                "/fixtures/polymarket-events.json"
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

        var objectMapper =
                PolymarketObjectMapperFactory.create();

        PolymarketProperties properties = testProperties();

        GammaPolymarketClient client =
                new GammaPolymarketClient(
                        transport,
                        objectMapper,
                        new GammaUriFactory(properties),
                        new GammaMapper(
                                new GammaArrayParser(objectMapper)
                        ),
                        properties.requestTimeout(),
                        Clock.fixed(
                                Instant.parse(
                                        "2026-07-15T10:00:00Z"
                                ),
                                ZoneOffset.UTC
                        )
                );

        var batch = client.fetchActiveEvents(
                100,
                0,
                "tag-ufc"
        );

        assertEquals(1, batch.events().size());

        var event = batch.events().getFirst();

        assertEquals("event-100", event.externalEventId());
        assertTrue(event.tags().contains("ufc"));
        assertEquals(1, event.markets().size());

        var market = event.markets().getFirst();

        assertTrue(market.orderBookEnabled());
        assertEquals(2, market.outcomes().size());

        assertEquals(
                "Fighter A",
                market.outcomes().getFirst().outcomeName()
        );

        assertEquals(
                "token-a",
                market.outcomes().getFirst().tokenId()
        );

        assertEquals(
                "0.58",
                market.outcomes()
                        .getFirst()
                        .displayedProbability()
                        .value()
                        .toPlainString()
        );
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
                     GammaPolymarketClientTest.class
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