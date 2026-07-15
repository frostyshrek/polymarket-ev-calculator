package com.ufcstudy.odds.provider;

import com.ufcstudy.odds.config.OddsApiProperties;
import com.ufcstudy.odds.config.OddsClientObjectMapperFactory;
import com.ufcstudy.odds.http.FakeHttpTransport;
import com.ufcstudy.odds.http.HttpTransportResponse;
import com.ufcstudy.odds.model.OddsFormat;
import com.ufcstudy.odds.model.SportsbookOddsBatch;
import com.ufcstudy.odds.model.SportsbookOddsRequest;
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
import static org.junit.jupiter.api.Assertions.assertFalse;

class TheOddsApiClientTest {

    @Test
    void fetchesAndMapsCurrentMmaOdds() throws IOException {
        String body = readFixture(
                "/fixtures/the-odds-api-mma.json"
        );

        FakeHttpTransport transport = new FakeHttpTransport();

        transport.enqueue(
                new HttpTransportResponse(
                        200,
                        body,
                        Map.of(
                                "x-requests-remaining",
                                List.of("499"),
                                "x-requests-used",
                                List.of("1"),
                                "x-requests-last",
                                List.of("1")
                        )
                )
        );

        OddsApiProperties properties = testProperties();

        TheOddsApiClient client = new TheOddsApiClient(
                transport,
                OddsClientObjectMapperFactory.create(),
                new OddsApiUriFactory(properties),
                new OddsApiMapper(),
                new OddsApiQuotaParser(),
                properties.requestTimeout(),
                Clock.fixed(
                        Instant.parse("2026-07-15T10:00:00Z"),
                        ZoneOffset.UTC
                )
        );

        SportsbookOddsBatch result = client.fetchCurrentOdds(
                new SportsbookOddsRequest(
                        properties.sportKey(),
                        properties.regions(),
                        properties.markets(),
                        OddsFormat.DECIMAL
                )
        );

        assertEquals("THE_ODDS_API", result.providerCode());
        assertEquals(1, result.events().size());
        assertEquals(499L, result.quota().requestsRemaining());

        var event = result.events().getFirst();

        assertEquals(
                "e46e328f688ff096aca6e5bb4ac96bbc",
                event.externalEventId()
        );
        assertEquals("Fighter A", event.participantA());
        assertEquals("Fighter B", event.participantB());
        assertEquals(1, event.bookmakers().size());

        var outcomes = event.bookmakers()
                .getFirst()
                .markets()
                .getFirst()
                .outcomes();

        assertEquals(2, outcomes.size());
        assertEquals("1.70", outcomes.getFirst()
                .decimalOdds()
                .value()
                .toPlainString());

        assertFalse(result.rawPayload().isBlank());
        assertEquals(1, transport.requests().size());
    }

    private static OddsApiProperties testProperties() {
        return new OddsApiProperties(
                true,
                URI.create("https://api.the-odds-api.com"),
                "test-key",
                "mma_mixed_martial_arts",
                List.of("uk"),
                List.of("h2h"),
                "decimal",
                "iso",
                Duration.ofSeconds(5),
                Duration.ofSeconds(15),
                Duration.ofMinutes(10),
                100,
                2
        );
    }

    private static String readFixture(String resource)
            throws IOException {

        try (var input =
                     TheOddsApiClientTest.class
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