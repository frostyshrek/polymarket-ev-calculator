package com.ufcstudy.settlement.service;

import com.ufcstudy.persistence.settlement.model.EventResolutionInsert;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class EventSettlementService {

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
        this.transactions = Objects.requireNonNull(transactions);
        this.resolutionRepository =
                Objects.requireNonNull(resolutionRepository);
        this.openPaperBetRepository =
                Objects.requireNonNull(openPaperBetRepository);
        this.settlementRepository =
                Objects.requireNonNull(settlementRepository);
        this.auditRepository =
                Objects.requireNonNull(auditRepository);
        this.resultResolver =
                Objects.requireNonNull(resultResolver);
        this.calculator = Objects.requireNonNull(calculator);
    }

    public EventSettlementResult settle(
            SettleEventCommand command
    ) {
        Objects.requireNonNull(command);

        return transactions.execute(status -> {
            validate(command);

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

            List<OpenPaperBetRecord> openBets =
                    openPaperBetRepository.findOpenByEventId(
                            command.sportingEventId()
                    );

            List<UUID> settledBetIds = new ArrayList<>();

            for (OpenPaperBetRecord record : openBets) {
                if (!record.sportingEventId().equals(
                        command.sportingEventId()
                )) {
                    throw new SettlementException(
                            "Paper bet belongs to another event: "
                                    + record.paperBetId()
                    );
                }

                OpenPaperBet bet = new OpenPaperBet(
                        record.paperBetId(),
                        record.sportingEventId(),
                        record.selectedParticipantId(),
                        record.stakeUnits(),
                        record.decimalOdds()
                );

                var result = resultResolver.resolve(
                        bet,
                        command.officialResultType(),
                        command.winningParticipantId()
                );

                var amounts = calculator.calculate(
                        result,
                        bet.stakeUnits(),
                        bet.decimalOdds()
                );

                int updated = settlementRepository.settle(
                        bet.paperBetId(),
                        result.name(),
                        amounts.grossReturnUnits(),
                        amounts.netProfitUnits(),
                        command.observedAt()
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
                        command.resolvedBy(),
                        command.observedAt()
                );

                settledBetIds.add(bet.paperBetId());
            }

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

    private void validate(SettleEventCommand command) {
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
}