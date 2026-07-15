package com.ufcstudy.odds.provider;

import com.ufcstudy.odds.config.OddsApiProperties;
import com.ufcstudy.odds.model.OddsFormat;
import com.ufcstudy.odds.model.SportsbookOddsRequest;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OddsApiUriFactoryTest {

    @Test
    void createsMmaMoneylineUri() {
        OddsApiProperties properties = new OddsApiProperties(
                true,
                URI.create("https://api.the-odds-api.com"),
                "secret-key",
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

        URI uri = new OddsApiUriFactory(properties)
                .currentOddsUri(
                        new SportsbookOddsRequest(
                                "mma_mixed_martial_arts",
                                List.of("uk"),
                                List.of("h2h"),
                                OddsFormat.DECIMAL
                        )
                );

        String value = uri.toString();

        assertTrue(value.startsWith(
                "https://api.the-odds-api.com"
                        + "/v4/sports/"
                        + "mma_mixed_martial_arts/odds?"
        ));

        assertTrue(value.contains("regions=uk"));
        assertTrue(value.contains("markets=h2h"));
        assertTrue(value.contains("oddsFormat=decimal"));
        assertTrue(value.contains("dateFormat=iso"));
    }
}