package com.ufcstudy.odds.provider;

import com.ufcstudy.odds.exception.SportsbookRateLimitException;
import com.ufcstudy.odds.model.OddsFormat;
import com.ufcstudy.odds.model.ProviderQuota;
import com.ufcstudy.odds.model.SportsbookOddsBatch;
import com.ufcstudy.odds.model.SportsbookOddsRequest;
import com.ufcstudy.odds.polling.RetryPolicy;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RetryingSportsbookOddsClientTest {

    @Test
    void retriesAfterRateLimit() {
        AtomicInteger attempts = new AtomicInteger();
        List<Duration> sleeps = new ArrayList<>();

        SportsbookOddsClient delegate =
                new SportsbookOddsClient() {
                    @Override
                    public String providerCode() {
                        return "TEST";
                    }

                    @Override
                    public SportsbookOddsBatch fetchCurrentOdds(
                            SportsbookOddsRequest request
                    ) {
                        if (attempts.incrementAndGet() == 1) {
                            throw new SportsbookRateLimitException(
                                    "Rate limited",
                                    Duration.ofSeconds(3)
                            );
                        }

                        return new SportsbookOddsBatch(
                                "TEST",
                                Instant.parse(
                                        "2026-07-15T10:00:00Z"
                                ),
                                List.of(),
                                new ProviderQuota(100L, 1L, 1L),
                                "[]"
                        );
                    }
                };

        RetryingSportsbookOddsClient client =
                new RetryingSportsbookOddsClient(
                        delegate,
                        new RetryPolicy(
                                2,
                                Duration.ofSeconds(2),
                                Duration.ofSeconds(10)
                        ),
                        sleeps::add
                );

        client.fetchCurrentOdds(
                new SportsbookOddsRequest(
                        "mma_mixed_martial_arts",
                        List.of("uk"),
                        List.of("h2h"),
                        OddsFormat.DECIMAL
                )
        );

        assertEquals(2, attempts.get());
        assertEquals(
                List.of(Duration.ofSeconds(3)),
                sleeps
        );
    }
}