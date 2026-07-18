package com.ufcstudy.console.settlement.service;

import com.ufcstudy.domain.settlement.OfficialResultType;
import com.ufcstudy.persistence.settlement.model.EventResolutionInsert;
import com.ufcstudy.persistence.settlement.model.FinalEventResolutionRecord;
import com.ufcstudy.persistence.settlement.model.OpenPaperBetRecord;
import com.ufcstudy.persistence.settlement.repository.EventResolutionRepository;
import com.ufcstudy.persistence.settlement.repository.OpenPaperBetRepository;
import com.ufcstudy.persistence.settlement.repository.PaperBetSettlementRepository;
import com.ufcstudy.persistence.settlement.repository.SettlementAuditRepository;
import com.ufcstudy.settlement.calculation.PaperBetResultResolver;
import com.ufcstudy.settlement.calculation.PaperBetSettlementCalculator;
import com.ufcstudy.settlement.model.EventSettlementResult;
import com.ufcstudy.settlement.model.OpenPaperBet;
import com.ufcstudy.settlement.model.SettleEventCommand;
import com.ufcstudy.settlement.service.SettlementException;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class EventSettlementService {

    private final TransactionTemplate transactions;
    private final EventResolutionRepository resolutionRepository;
    private final OpenPaperBetRepository openPaperBetRepository;
    private final PaperBetSettlementRepository settlementRepository;
    private final SettlementAuditRepository auditRepository;
    private final PaperBetResultResolver resultResolver;
    private final PaperBetSettlementCalculator calculator;

    public EventSettlementService(
            TransactionTemplate transactions,
            EventResolutionRepository resolutionRepository,
            OpenPaperBetRepository openPaperBetRepository,
            PaperBetSettlementRepository settlementRepository,
            SettlementAuditRepository auditRepository,
            PaperBetResultResolver resultResolver,
            PaperBetSettlementCalculator calculator
    ) {
        this.transactions =
                Objects.requireNonNull(
                        transactions,
                        "Transaction template cannot be null"
                );

        this.resolutionRepository =
                Objects.requireNonNull(
                        resolutionRepository,
                        "Resolution repository cannot be null"
                );

        this.openPaperBetRepository =
                Objects.requireNonNull(
                        openPaperBetRepository,
                        "Open paper-bet repository cannot be null"
                );

        this.settlementRepository =
                Objects.requireNonNull(
                        settlementRepository,
                        "Settlement repository cannot be null"
                );

        this.auditRepository =
                Objects.requireNonNull(
                        auditRepository,
                        "Audit repository cannot be null"
                );

        this.resultResolver =
                Objects.requireNonNull(
                        resultResolver,
                        "Result resolver cannot be null"
                );

        this.calculator =
                Objects.requireNonNull(
                        calculator,
                        "Settlement calculator cannot be null"
                );
    }

    /**
     * Records a new final event resolution and immediately settles
     * all open paper bets for the event.
     *
     * Use this method when no final event_resolution row exists yet.
     */
    public EventSettlementResult settle(
            SettleEventCommand command
    ) {
        Objects.requireNonNull(
                command,
                "Settlement command cannot be null"
        );

        return transactions.execute(status -> {
            validateNewResolution(command);

            UUID resolutionId = UUID.randomUUID();

            resolutionRepository.insert(
                    new EventResolutionInsert(
                            resolutionId,
                            command.sportingEventId(),
                            command.winningParticipantId(),
                            command.officialResultType().name(),
                            command.officialResultText(),
                            command.resultSourceId(),
                            command.sourceExternalResultId(),
                            command.officialResultAt(),
                            command.observedAt(),
                            true,
                            """
                            {
                              "resolutionStatus": "CONFIRMED",
                              "settlementVersion": "settlement-v1"
                            }
                            """
                    )
            );

            List<UUID> settledBetIds =
                    settleOpenBets(
                            command.sportingEventId(),
                            command.officialResultType(),
                            command.winningParticipantId(),
                            command.resolvedBy(),
                            command.observedAt()
                    );

            auditRepository.recordEventResolution(
                    resolutionId,
                    command.sportingEventId(),
                    command.officialResultType().name(),
                    command.resolvedBy(),
                    command.observedAt()
            );

            return new EventSettlementResult(
                    resolutionId,
                    command.sportingEventId(),
                    settledBetIds.size(),
                    settledBetIds
            );
        });
    }

    /**
     * Settles open paper bets using a final event_resolution row
     * that was already recorded by console Step 7.
     */
    public EventSettlementResult settleRecordedResolution(
            UUID sportingEventId,
            String resolvedBy
    ) {
        Objects.requireNonNull(
                sportingEventId,
                "Sporting event ID cannot be null"
        );

        Objects.requireNonNull(
                resolvedBy,
                "Resolved by cannot be null"
        );

        if (resolvedBy.isBlank()) {
            throw new IllegalArgumentException(
                    "Resolved by cannot be blank"
            );
        }

        return transactions.execute(status -> {
            FinalEventResolutionRecord resolution =
                    resolutionRepository
                            .findFinalByEventId(
                                    sportingEventId
                            )
                            .orElseThrow(() ->
                                    new SettlementException(
                                            "No final resolution exists "
                                                    + "for event: "
                                                    + sportingEventId
                                    )
                            );

            OfficialResultType resultType =
                    parseResultType(
                            resolution.officialResultType()
                    );

            List<UUID> settledBetIds =
                    settleOpenBets(
                            sportingEventId,
                            resultType,
                            resolution.winningParticipantId(),
                            resolvedBy.trim(),
                            resolution.observedAt()
                    );

            return new EventSettlementResult(
                    resolution.resolutionId(),
                    sportingEventId,
                    settledBetIds.size(),
                    settledBetIds
            );
        });
    }

    private List<UUID> settleOpenBets(
            UUID sportingEventId,
            OfficialResultType officialResultType,
            UUID winningParticipantId,
            String resolvedBy,
            Instant settledAt
    ) {
        List<OpenPaperBetRecord> openBets =
                openPaperBetRepository.findOpenByEventId(
                        sportingEventId
                );

        List<UUID> settledBetIds =
                new ArrayList<>();

        for (OpenPaperBetRecord record : openBets) {
            validateOpenBetEvent(
                    record,
                    sportingEventId
            );

            OpenPaperBet bet =
                    new OpenPaperBet(
                            record.paperBetId(),
                            record.sportingEventId(),
                            record.selectedParticipantId(),
                            record.stakeUnits(),
                            record.decimalOdds()
                    );

            var result =
                    resultResolver.resolve(
                            bet,
                            officialResultType,
                            winningParticipantId
                    );

            var amounts =
                    calculator.calculate(
                            result,
                            bet.stakeUnits(),
                            bet.decimalOdds()
                    );

            int updated =
                    settlementRepository.settle(
                            bet.paperBetId(),
                            result.name(),
                            amounts.grossReturnUnits(),
                            amounts.netProfitUnits(),
                            settledAt
                    );

            if (updated != 1) {
                throw new SettlementException(
                        "Paper bet could not be settled: "
                                + bet.paperBetId()
                );
            }

            auditRepository.recordPaperBetSettlement(
                    bet.paperBetId(),
                    result.name(),
                    amounts.grossReturnUnits(),
                    amounts.netProfitUnits(),
                    resolvedBy,
                    settledAt
            );

            settledBetIds.add(
                    bet.paperBetId()
            );
        }

        return List.copyOf(settledBetIds);
    }

    private void validateNewResolution(
            SettleEventCommand command
    ) {
        if (!resolutionRepository.sportingEventExists(
                command.sportingEventId()
        )) {
            throw new SettlementException(
                    "Sporting event does not exist: "
                            + command.sportingEventId()
            );
        }

        if (resolutionRepository.finalResolutionExists(
                command.sportingEventId()
        )) {
            throw new SettlementException(
                    "A final resolution already exists "
                            + "for this event"
            );
        }

        if (command.winningParticipantId() != null
                && !resolutionRepository
                .participantBelongsToEvent(
                        command.sportingEventId(),
                        command.winningParticipantId()
                )) {
            throw new SettlementException(
                    "Winning participant does not belong "
                            + "to this event"
            );
        }
    }

    private static void validateOpenBetEvent(
            OpenPaperBetRecord record,
            UUID expectedEventId
    ) {
        if (!record.sportingEventId().equals(
                expectedEventId
        )) {
            throw new SettlementException(
                    "Paper bet belongs to another event: "
                            + record.paperBetId()
            );
        }
    }

    private static OfficialResultType parseResultType(
            String resultType
    ) {
        try {
            return OfficialResultType.valueOf(
                    resultType
            );
        } catch (IllegalArgumentException exception) {
            throw new SettlementException(
                    "Unsupported official result type: "
                            + resultType
            );
        }
    }
}