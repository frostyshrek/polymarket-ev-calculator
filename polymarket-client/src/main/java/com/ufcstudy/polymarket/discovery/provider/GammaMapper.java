package com.ufcstudy.polymarket.discovery.provider;

import com.ufcstudy.domain.value.Probability;
import com.ufcstudy.polymarket.discovery.dto.GammaEventDto;
import com.ufcstudy.polymarket.discovery.dto.GammaMarketDto;
import com.ufcstudy.polymarket.discovery.dto.GammaTagDto;
import com.ufcstudy.polymarket.discovery.model.PolymarketEvent;
import com.ufcstudy.polymarket.discovery.model.PolymarketMarket;
import com.ufcstudy.polymarket.discovery.model.PolymarketOutcomeToken;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class GammaMapper {

    private final GammaArrayParser arrayParser;

    public GammaMapper(GammaArrayParser arrayParser) {
        this.arrayParser = Objects.requireNonNull(arrayParser);
    }

    public List<PolymarketEvent> map(List<GammaEventDto> source) {
        Objects.requireNonNull(source);

        return source.stream()
                .map(this::mapEvent)
                .toList();
    }

    private PolymarketEvent mapEvent(GammaEventDto source) {
        requireText(source.id(), "Event ID");
        requireText(source.title(), "Event title");

        Set<String> tags = safeList(source.tags())
                .stream()
                .map(GammaTagDto::slug)
                .filter(Objects::nonNull)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toUnmodifiableSet());

        List<PolymarketMarket> markets =
                safeList(source.markets())
                        .stream()
                        .map(this::mapMarket)
                        .toList();

        return new PolymarketEvent(
                source.id(),
                source.slug(),
                source.title(),
                source.description(),
                source.startDate(),
                source.endDate(),
                source.active(),
                source.closed(),
                tags,
                markets
        );
    }

    private PolymarketMarket mapMarket(GammaMarketDto source) {
        requireText(source.id(), "Market ID");
        requireText(source.question(), "Market question");

        List<String> outcomes = arrayParser.parseStrings(
                source.outcomes(),
                "outcomes"
        );

        List<String> prices = arrayParser.parseStrings(
                source.outcomePrices(),
                "outcomePrices"
        );

        List<String> tokenIds = arrayParser.parseStrings(
                source.clobTokenIds(),
                "clobTokenIds"
        );

        if (outcomes.size() != tokenIds.size()) {
            throw new IllegalArgumentException(
                    "Outcome count does not match token count for market "
                            + source.id()
            );
        }

        if (!prices.isEmpty() && prices.size() != outcomes.size()) {
            throw new IllegalArgumentException(
                    "Outcome count does not match price count for market "
                            + source.id()
            );
        }

        List<PolymarketOutcomeToken> mappedOutcomes =
                new ArrayList<>(outcomes.size());

        for (int index = 0; index < outcomes.size(); index++) {
            Probability displayedProbability =
                    prices.isEmpty()
                            ? null
                            : parseProbability(prices.get(index));

            mappedOutcomes.add(
                    new PolymarketOutcomeToken(
                            outcomes.get(index),
                            tokenIds.get(index),
                            displayedProbability
                    )
            );
        }

        return new PolymarketMarket(
                source.id(),
                source.conditionId(),
                source.slug(),
                source.question(),
                source.resolutionSource(),
                source.endDate(),
                source.active(),
                source.closed(),
                source.enableOrderBook(),
                mappedOutcomes
        );
    }

    private static Probability parseProbability(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Probability.of(new BigDecimal(value));
    }

    private static void requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }
    }

    private static <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}