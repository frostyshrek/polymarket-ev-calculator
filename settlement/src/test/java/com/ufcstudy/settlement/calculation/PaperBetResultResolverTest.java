package com.ufcstudy.settlement.calculation;

import com.ufcstudy.domain.paperbetting.PaperBetResult;
import com.ufcstudy.domain.settlement.OfficialResultType;
import com.ufcstudy.settlement.model.OpenPaperBet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaperBetResultResolverTest {

    private final PaperBetResultResolver resolver =
            new PaperBetResultResolver();

    private final UUID selectedParticipantId =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    private final UUID otherParticipantId =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000002"
            );

    private final OpenPaperBet bet = new OpenPaperBet(
            UUID.randomUUID(),
            UUID.randomUUID(),
            selectedParticipantId,
            BigDecimal.ONE,
            new BigDecimal("1.90")
    );

    @Test
    void selectedParticipantWinProducesWin() {
        assertEquals(
                PaperBetResult.WIN,
                resolver.resolve(
                        bet,
                        OfficialResultType.PARTICIPANT_WIN,
                        selectedParticipantId
                )
        );
    }

    @Test
    void opposingParticipantWinProducesLoss() {
        assertEquals(
                PaperBetResult.LOSS,
                resolver.resolve(
                        bet,
                        OfficialResultType.PARTICIPANT_WIN,
                        otherParticipantId
                )
        );
    }

    @Test
    void drawProducesVoid() {
        assertEquals(
                PaperBetResult.VOID,
                resolver.resolve(
                        bet,
                        OfficialResultType.DRAW,
                        null
                )
        );
    }

    @Test
    void noContestProducesVoid() {
        assertEquals(
                PaperBetResult.VOID,
                resolver.resolve(
                        bet,
                        OfficialResultType.NO_CONTEST,
                        null
                )
        );
    }

    @Test
    void cancellationProducesVoid() {
        assertEquals(
                PaperBetResult.VOID,
                resolver.resolve(
                        bet,
                        OfficialResultType.CANCELLED,
                        null
                )
        );
    }

    @Test
    void postponementProducesVoid() {
        assertEquals(
                PaperBetResult.VOID,
                resolver.resolve(
                        bet,
                        OfficialResultType.POSTPONED,
                        null
                )
        );
    }
}