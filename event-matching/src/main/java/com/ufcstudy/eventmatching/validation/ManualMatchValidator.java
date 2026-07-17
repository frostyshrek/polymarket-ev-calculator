package com.ufcstudy.eventmatching.validation;

import com.ufcstudy.domain.matching.SettlementCompatibility;
import com.ufcstudy.eventmatching.model.ManualEventMatchCommand;
import com.ufcstudy.eventmatching.model.OutcomePair;
import com.ufcstudy.persistence.matching.repository.MatchValidationRepository;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ManualMatchValidator {

    private final MatchValidationRepository repository;

    public ManualMatchValidator(
            MatchValidationRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void validate(ManualEventMatchCommand command) {
        Objects.requireNonNull(command);

        if (command.settlementCompatibility()
                != SettlementCompatibility.EXACT) {
            throw new IllegalArgumentException(
                    "UFC-EV-v1.0 manual approvals require EXACT settlement compatibility"
            );
        }

        if (command.sportsbookMarketId()
                .equals(command.predictionMarketId())) {
            throw new IllegalArgumentException(
                    "Sportsbook and prediction market IDs must differ"
            );
        }

        if (!repository.isEligibleMoneylineMarket(
                command.sportsbookMarketId()
        )) {
            throw new IllegalArgumentException(
                    "Sportsbook market is not an eligible pre-fight moneyline market"
            );
        }

        if (!repository.isEligibleMoneylineMarket(
                command.predictionMarketId()
        )) {
            throw new IllegalArgumentException(
                    "Prediction market is not an eligible pre-fight moneyline market"
            );
        }

        Set<UUID> sportsbookOutcomes = new HashSet<>();
        Set<UUID> predictionOutcomes = new HashSet<>();

        for (OutcomePair pair : command.outcomePairs()) {
            if (!repository.outcomeBelongsToMarket(
                    pair.sportsbookOutcomeId(),
                    command.sportsbookMarketId()
            )) {
                throw new IllegalArgumentException(
                        "Sportsbook outcome does not belong to the selected market"
                );
            }

            if (!repository.outcomeBelongsToMarket(
                    pair.predictionMarketOutcomeId(),
                    command.predictionMarketId()
            )) {
                throw new IllegalArgumentException(
                        "Prediction outcome does not belong to the selected market"
                );
            }

            sportsbookOutcomes.add(pair.sportsbookOutcomeId());
            predictionOutcomes.add(
                    pair.predictionMarketOutcomeId()
            );
        }

        if (sportsbookOutcomes.size() != 2) {
            throw new IllegalArgumentException(
                    "Sportsbook outcomes must be distinct"
            );
        }

        if (predictionOutcomes.size() != 2) {
            throw new IllegalArgumentException(
                    "Prediction-market outcomes must be distinct"
            );
        }

        String fighterOne = command.outcomePairs()
                .get(0)
                .fighter()
                .canonicalName();

        String fighterTwo = command.outcomePairs()
                .get(1)
                .fighter()
                .canonicalName();

        if (fighterOne.equalsIgnoreCase(fighterTwo)) {
            throw new IllegalArgumentException(
                    "The two canonical fighters must be different"
            );
        }
    }
}