package com.ufcstudy.odds.provider;

import com.ufcstudy.domain.value.DecimalOdds;
import com.ufcstudy.odds.dto.OddsApiBookmakerDto;
import com.ufcstudy.odds.dto.OddsApiEventDto;
import com.ufcstudy.odds.dto.OddsApiMarketDto;
import com.ufcstudy.odds.dto.OddsApiOutcomeDto;
import com.ufcstudy.odds.model.SportsbookBookmaker;
import com.ufcstudy.odds.model.SportsbookEvent;
import com.ufcstudy.odds.model.SportsbookMarket;
import com.ufcstudy.odds.model.SportsbookOutcome;

import java.util.List;
import java.util.Objects;

public final class OddsApiMapper {

    public List<SportsbookEvent> map(List<OddsApiEventDto> source) {
        Objects.requireNonNull(source);

        return source.stream()
                .map(this::mapEvent)
                .toList();
    }

    private SportsbookEvent mapEvent(OddsApiEventDto source) {
        requireText(source.id(), "Event ID");
        requireText(source.sportKey(), "Sport key");
        requireText(source.homeTeam(), "Home team");
        requireText(source.awayTeam(), "Away team");

        if (source.commenceTime() == null) {
            throw new IllegalArgumentException(
                    "Event commence time cannot be null"
            );
        }

        return new SportsbookEvent(
                "THE_ODDS_API",
                source.id(),
                source.sportKey(),
                source.sportTitle(),
                source.commenceTime(),
                source.homeTeam(),
                source.awayTeam(),
                safeList(source.bookmakers())
                        .stream()
                        .map(this::mapBookmaker)
                        .toList()
        );
    }

    private SportsbookBookmaker mapBookmaker(
            OddsApiBookmakerDto source
    ) {
        requireText(source.key(), "Bookmaker key");
        requireText(source.title(), "Bookmaker title");

        if (source.lastUpdate() == null) {
            throw new IllegalArgumentException(
                    "Bookmaker update time cannot be null"
            );
        }

        return new SportsbookBookmaker(
                source.key(),
                source.title(),
                source.lastUpdate(),
                safeList(source.markets())
                        .stream()
                        .map(this::mapMarket)
                        .toList()
        );
    }

    private SportsbookMarket mapMarket(OddsApiMarketDto source) {
        requireText(source.key(), "Market key");

        if (source.lastUpdate() == null) {
            throw new IllegalArgumentException(
                    "Market update time cannot be null"
            );
        }

        return new SportsbookMarket(
                source.key(),
                source.lastUpdate(),
                safeList(source.outcomes())
                        .stream()
                        .map(this::mapOutcome)
                        .toList()
        );
    }

    private SportsbookOutcome mapOutcome(OddsApiOutcomeDto source) {
        requireText(source.name(), "Outcome name");

        if (source.price() == null) {
            throw new IllegalArgumentException(
                    "Outcome price cannot be null"
            );
        }

        return new SportsbookOutcome(
                source.name(),
                DecimalOdds.of(source.price())
        );
    }

    private static void requireText(String value, String fieldName) {
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