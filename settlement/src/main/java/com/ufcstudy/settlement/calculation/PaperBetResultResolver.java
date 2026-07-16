package com.ufcstudy.settlement.calculation;

import com.ufcstudy.domain.paperbetting.PaperBetResult;
import com.ufcstudy.domain.settlement.OfficialResultType;
import com.ufcstudy.settlement.model.OpenPaperBet;

import java.util.Objects;
import java.util.UUID;

public final class PaperBetResultResolver {

    public PaperBetResult resolve(
            OpenPaperBet bet,
            OfficialResultType officialResultType,
            UUID winningParticipantId
    ) {
        Objects.requireNonNull(bet);
        Objects.requireNonNull(officialResultType);

        return switch (officialResultType) {
            case PARTICIPANT_WIN -> {
                Objects.requireNonNull(
                        winningParticipantId,
                        "Winning participant is required"
                );

                yield bet.selectedParticipantId()
                        .equals(winningParticipantId)
                        ? PaperBetResult.WIN
                        : PaperBetResult.LOSS;
            }

            case DRAW,
                 NO_CONTEST,
                 CANCELLED,
                 POSTPONED -> PaperBetResult.VOID;
        };
    }
}