package com.ufcstudy.matching.service;

import com.ufcstudy.matching.model.ManualEventMatchCommand;
import com.ufcstudy.matching.model.ManualEventMatchResult;
import com.ufcstudy.domain.matching.MappingStatus;
import com.ufcstudy.matching.normalization.ParticipantNameNormalizer;
import com.ufcstudy.matching.validation.ManualMatchValidator;
import com.ufcstudy.persistence.matching.repository.CompetitionRepository;
import com.ufcstudy.persistence.matching.repository.EventParticipantRepository;
import com.ufcstudy.persistence.matching.repository.MarketMappingRepository;
import com.ufcstudy.persistence.matching.repository.MatchingAuditRepository;
import com.ufcstudy.persistence.matching.repository.ParticipantAliasRepository;
import com.ufcstudy.persistence.matching.repository.ParticipantRepository;
import com.ufcstudy.persistence.matching.repository.SourceEventReferenceRepository;
import com.ufcstudy.persistence.matching.repository.SportRepository;
import com.ufcstudy.persistence.matching.repository.SportingEventRepository;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ManualEventMatchingService {

    private final TransactionTemplate transactions;
    private final ManualMatchValidator validator;
    private final SportRepository sportRepository;
    private final CompetitionRepository competitionRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantAliasRepository aliasRepository;
    private final SportingEventRepository eventRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final SourceEventReferenceRepository sourceEventRepository;
    private final MarketMappingRepository mappingRepository;
    private final MatchingAuditRepository auditRepository;
    private final ParticipantNameNormalizer normalizer;
    private final Clock clock;

    public ManualEventMatchingService(
            TransactionTemplate transactions,
            ManualMatchValidator validator,
            SportRepository sportRepository,
            CompetitionRepository competitionRepository,
            ParticipantRepository participantRepository,
            ParticipantAliasRepository aliasRepository,
            SportingEventRepository eventRepository,
            EventParticipantRepository eventParticipantRepository,
            SourceEventReferenceRepository sourceEventRepository,
            MarketMappingRepository mappingRepository,
            MatchingAuditRepository auditRepository,
            ParticipantNameNormalizer normalizer,
            Clock clock
    ) {
        this.transactions = Objects.requireNonNull(transactions);
        this.validator = Objects.requireNonNull(validator);
        this.sportRepository = Objects.requireNonNull(sportRepository);
        this.competitionRepository =
                Objects.requireNonNull(competitionRepository);
        this.participantRepository =
                Objects.requireNonNull(participantRepository);
        this.aliasRepository = Objects.requireNonNull(aliasRepository);
        this.eventRepository = Objects.requireNonNull(eventRepository);
        this.eventParticipantRepository =
                Objects.requireNonNull(eventParticipantRepository);
        this.sourceEventRepository =
                Objects.requireNonNull(sourceEventRepository);
        this.mappingRepository =
                Objects.requireNonNull(mappingRepository);
        this.auditRepository =
                Objects.requireNonNull(auditRepository);
        this.normalizer = Objects.requireNonNull(normalizer);
        this.clock = Objects.requireNonNull(clock);
    }

    public ManualEventMatchResult approve(
            ManualEventMatchCommand command
    ) {
        validator.validate(command);

        return transactions.execute(status -> {
            var approvedAt = clock.instant();

            UUID sportId = sportRepository.findOrCreate(
                    "MMA",
                    "Mixed Martial Arts"
            );

            UUID competitionId =
                    competitionRepository.findOrCreateUfc(
                            sportId
                    );

            List<UUID> participantIds = new ArrayList<>();

            for (var pair : command.outcomePairs()) {
                var fighter = pair.fighter();

                UUID participantId =
                        participantRepository.findOrCreate(
                                sportId,
                                fighter.canonicalName(),
                                normalizer.normalize(
                                        fighter.canonicalName()
                                )
                        );

                participantIds.add(participantId);

                aliasRepository.insertIfMissing(
                        participantId,
                        command.sportsbookDataSourceId(),
                        fighter.sportsbookName(),
                        normalizer.normalize(
                                fighter.sportsbookName()
                        )
                );

                aliasRepository.insertIfMissing(
                        participantId,
                        command.polymarketDataSourceId(),
                        fighter.polymarketName(),
                        normalizer.normalize(
                                fighter.polymarketName()
                        )
                );
            }

            UUID eventId = eventRepository.insert(
                    sportId,
                    competitionId,
                    command.canonicalEventName(),
                    command.scheduledStartTime()
            );

            eventParticipantRepository.insert(
                    eventId,
                    participantIds.get(0),
                    1
            );

            eventParticipantRepository.insert(
                    eventId,
                    participantIds.get(1),
                    2
            );

            sourceEventRepository.insert(
                    eventId,
                    command.sportsbookDataSourceId(),
                    command.sportsbookExternalEventId(),
                    command.sportsbookExternalEventName(),
                    approvedAt
            );

            sourceEventRepository.insert(
                    eventId,
                    command.polymarketDataSourceId(),
                    command.polymarketExternalEventId(),
                    command.polymarketExternalEventName(),
                    approvedAt
            );

            List<UUID> mappingIds = new ArrayList<>();

            for (var pair : command.outcomePairs()) {
                UUID mappingId =
                        mappingRepository.insertManualApproval(
                                eventId,
                                command.sportsbookMarketId(),
                                command.predictionMarketId(),
                                pair.sportsbookOutcomeId(),
                                pair.predictionMarketOutcomeId(),
                                command.settlementCompatibility(),
                                command.approvalNotes(),
                                command.approvedBy(),
                                approvedAt
                        );

                mappingIds.add(mappingId);

                auditRepository.recordApproval(
                        mappingId,
                        "Manually matched "
                                + pair.fighter().canonicalName()
                                + " across sportsbook and Polymarket outcomes",
                        command.approvedBy(),
                        approvedAt
                );
            }

            return new ManualEventMatchResult(
                    eventId,
                    participantIds,
                    mappingIds,
                    MappingStatus.APPROVED_MANUAL
            );
        });
    }
}