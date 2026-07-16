package com.ufcstudy.persistence.market.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public final class SourceOutcomeRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final DeterministicUuidFactory uuidFactory;

    public SourceOutcomeRepository(
            NamedParameterJdbcTemplate jdbc,
            DeterministicUuidFactory uuidFactory
    ) {
        this.jdbc = Objects.requireNonNull(jdbc);
        this.uuidFactory = Objects.requireNonNull(uuidFactory);
    }

    public UUID upsert(
            UUID sourceMarketId,
            String externalOutcomeId,
            String outcomeName,
            String outcomeType
    ) {
        UUID id = uuidFactory.from(
                sourceMarketId.toString(),
                externalOutcomeId
        );

        jdbc.update(
                """
                INSERT INTO ufc_study.source_market_outcome (
                    id,
                    source_market_id,
                    external_outcome_id,
                    outcome_name,
                    normalized_outcome_name,
                    outcome_type
                )
                VALUES (
                    :id,
                    :sourceMarketId,
                    :externalOutcomeId,
                    :outcomeName,
                    :normalizedOutcomeName,
                    :outcomeType
                )
                ON CONFLICT (
                    source_market_id,
                    external_outcome_id
                )
                DO UPDATE SET
                    outcome_name = EXCLUDED.outcome_name,
                    normalized_outcome_name =
                        EXCLUDED.normalized_outcome_name,
                    outcome_type = EXCLUDED.outcome_type
                """,
                new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue(
                                "sourceMarketId",
                                sourceMarketId
                        )
                        .addValue(
                                "externalOutcomeId",
                                externalOutcomeId
                        )
                        .addValue("outcomeName", outcomeName)
                        .addValue(
                                "normalizedOutcomeName",
                                normalize(outcomeName)
                        )
                        .addValue("outcomeType", outcomeType)
        );

        return id;
    }

    private static String normalize(String value) {
        String normalized = Normalizer.normalize(
                value,
                Normalizer.Form.NFKD
        );

        return normalized
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}