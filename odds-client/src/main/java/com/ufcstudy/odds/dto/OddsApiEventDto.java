package com.ufcstudy.odds.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OddsApiEventDto(
        String id,

        @JsonProperty("sport_key")
        String sportKey,

        @JsonProperty("sport_title")
        String sportTitle,

        @JsonProperty("commence_time")
        Instant commenceTime,

        @JsonProperty("home_team")
        String homeTeam,

        @JsonProperty("away_team")
        String awayTeam,

        List<OddsApiBookmakerDto> bookmakers
) {
}