package com.ufcstudy.paperbetting.service;

import com.ufcstudy.domain.paperbetting.PaperBetStatus;
import com.ufcstudy.domain.paperbetting.StakeMethod;
import com.ufcstudy.paperbetting.model.PaperBetPlacementRequest;
import com.ufcstudy.paperbetting.model.PaperBetPlacementStatus;
import com.ufcstudy.persistence.paperbetting.model.PaperBetCandidateRecord;
import com.ufcstudy.persistence.paperbetting.model.PaperBetInsert;
import com.ufcstudy.persistence.paperbetting.repository.PaperBetCandidateRepository;
import com.ufcstudy.persistence.paperbetting.repository.PaperBetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaperBetPlacementServiceTest {

    private static final UUID OPPORTUNITY_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    private static final UUID STRATEGY_VERSION_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000002"
            );

    private static final UUID MARKET_MAPPING_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000003"
            );

    private static final UUID SPORTING_EVENT_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000004"
            );

    private static final UUID SPORTSBOOK_MARKET_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000005"
            );

    private static final UUID SPORTSBOOK_OUTCOME_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000006"
            );

    private static final UUID PREDICTION_OUTCOME_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000007"
            );

    @Mock
    private TransactionTemplate transactions;

    @Mock
    private PaperBetCandidateRepository candidateRepository;

    @Mock
    private PaperBetRepository paperBetRepository;

    private PaperBetPlacementService service;

    @BeforeEach
    void setUp() {
        /*
         * TransactionTemplate normally receives a callback and executes it
         * within a database transaction. In this unit test, run the callback
         * immediately using a mocked transaction status.
         */
        when(transactions.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback =
                            invocation.getArgument(0);

                    TransactionStatus status =
                            mock(TransactionStatus.class);

                    return callback.doInTransaction(status);
                });

        service = new PaperBetPlacementService(
                transactions,
                candidateRepository,
                paperBetRepository
        );
    }

    @Test
    void placesQualifiedOpportunity() {
        PaperBetCandidateRecord candidate =
                qualifiedCandidate();

        when(candidateRepository.findByOpportunityId(
                OPPORTUNITY_ID
        )).thenReturn(Optional.of(candidate));

        when(paperBetRepository.existsForOpportunity(
                OPPORTUNITY_ID
        )).thenReturn(false);

        when(paperBetRepository.officialEntryExists(
                STRATEGY_VERSION_ID,
                SPORTING_EVENT_ID,
                SPORTSBOOK_OUTCOME_ID,
                "test_book",
                StakeMethod.FLAT_ONE_UNIT.name()
        )).thenReturn(false);

        var result = service.place(
                new PaperBetPlacementRequest(OPPORTUNITY_ID)
        );

        assertEquals(
                PaperBetPlacementStatus.PLACED,
                result.status()
        );

        assertNotNull(result.paperBetId());

        assertEquals(
                0,
                result.stakeUnits().compareTo(
                        new BigDecimal("1.00000000")
                )
        );

        ArgumentCaptor<PaperBetInsert> captor =
                ArgumentCaptor.forClass(PaperBetInsert.class);

        verify(paperBetRepository).insert(captor.capture());

        PaperBetInsert inserted = captor.getValue();

        assertEquals(
                OPPORTUNITY_ID,
                inserted.opportunityId()
        );

        assertEquals(
                STRATEGY_VERSION_ID,
                inserted.strategyVersionId()
        );

        assertEquals(
                SPORTING_EVENT_ID,
                inserted.sportingEventId()
        );

        assertEquals(
                StakeMethod.FLAT_ONE_UNIT.name(),
                inserted.stakeMethod()
        );

        assertEquals(
                PaperBetStatus.OPEN.name(),
                inserted.betStatus()
        );

        assertEquals(
                0,
                inserted.stakeUnits().compareTo(
                        new BigDecimal("1.00000000")
                )
        );

        assertEquals(
                0,
                inserted.decimalOdds().compareTo(
                        new BigDecimal("1.90")
                )
        );
    }

    @Test
    void rejectsNonQualifiedOpportunity() {
        PaperBetCandidateRecord candidate =
                candidate(
                        "REJECTED",
                        "APPROVED_MANUAL",
                        "EXACT"
                );

        when(candidateRepository.findByOpportunityId(
                OPPORTUNITY_ID
        )).thenReturn(Optional.of(candidate));

        PaperBetPlacementException exception =
                assertThrows(
                        PaperBetPlacementException.class,
                        () -> service.place(
                                new PaperBetPlacementRequest(
                                        OPPORTUNITY_ID
                                )
                        )
                );

        assertEquals(
                "Only QUALIFIED opportunities can create paper bets",
                exception.getMessage()
        );

        verifyNoInteractions(paperBetRepository);
    }

    @Test
    void rejectsUnapprovedMapping() {
        PaperBetCandidateRecord candidate =
                candidate(
                        "QUALIFIED",
                        "PENDING_REVIEW",
                        "EXACT"
                );

        when(candidateRepository.findByOpportunityId(
                OPPORTUNITY_ID
        )).thenReturn(Optional.of(candidate));

        PaperBetPlacementException exception =
                assertThrows(
                        PaperBetPlacementException.class,
                        () -> service.place(
                                new PaperBetPlacementRequest(
                                        OPPORTUNITY_ID
                                )
                        )
                );

        assertEquals(
                "Opportunity mapping is not approved",
                exception.getMessage()
        );

        verifyNoInteractions(paperBetRepository);
    }

    @Test
    void rejectsNonExactSettlementCompatibility() {
        PaperBetCandidateRecord candidate =
                candidate(
                        "QUALIFIED",
                        "APPROVED_MANUAL",
                        "AMBIGUOUS"
                );

        when(candidateRepository.findByOpportunityId(
                OPPORTUNITY_ID
        )).thenReturn(Optional.of(candidate));

        PaperBetPlacementException exception =
                assertThrows(
                        PaperBetPlacementException.class,
                        () -> service.place(
                                new PaperBetPlacementRequest(
                                        OPPORTUNITY_ID
                                )
                        )
                );

        assertEquals(
                "Settlement compatibility must be EXACT",
                exception.getMessage()
        );

        verifyNoInteractions(paperBetRepository);
    }

    @Test
    void returnsAlreadyPlacedWhenOpportunityHasPaperBet() {
        PaperBetCandidateRecord candidate =
                qualifiedCandidate();

        when(candidateRepository.findByOpportunityId(
                OPPORTUNITY_ID
        )).thenReturn(Optional.of(candidate));

        when(paperBetRepository.existsForOpportunity(
                OPPORTUNITY_ID
        )).thenReturn(true);

        var result = service.place(
                new PaperBetPlacementRequest(OPPORTUNITY_ID)
        );

        assertEquals(
                PaperBetPlacementStatus
                        .ALREADY_PLACED_FOR_OPPORTUNITY,
                result.status()
        );

        assertNull(result.paperBetId());
        assertNull(result.stakeUnits());

        verify(
                paperBetRepository,
                never()
        ).officialEntryExists(
                any(),
                any(),
                any(),
                any(),
                any()
        );

        verify(
                paperBetRepository,
                never()
        ).insert(any());
    }

    @Test
    void returnsOfficialEntryExistsWhenEarlierBetExists() {
        PaperBetCandidateRecord candidate =
                qualifiedCandidate();

        when(candidateRepository.findByOpportunityId(
                OPPORTUNITY_ID
        )).thenReturn(Optional.of(candidate));

        when(paperBetRepository.existsForOpportunity(
                OPPORTUNITY_ID
        )).thenReturn(false);

        when(paperBetRepository.officialEntryExists(
                STRATEGY_VERSION_ID,
                SPORTING_EVENT_ID,
                SPORTSBOOK_OUTCOME_ID,
                "test_book",
                StakeMethod.FLAT_ONE_UNIT.name()
        )).thenReturn(true);

        var result = service.place(
                new PaperBetPlacementRequest(OPPORTUNITY_ID)
        );

        assertEquals(
                PaperBetPlacementStatus
                        .OFFICIAL_ENTRY_ALREADY_EXISTS,
                result.status()
        );

        assertNull(result.paperBetId());
        assertNull(result.stakeUnits());

        verify(
                paperBetRepository,
                never()
        ).insert(any());
    }

    @Test
    void rejectsMissingOpportunity() {
        when(candidateRepository.findByOpportunityId(
                OPPORTUNITY_ID
        )).thenReturn(Optional.empty());

        PaperBetPlacementException exception =
                assertThrows(
                        PaperBetPlacementException.class,
                        () -> service.place(
                                new PaperBetPlacementRequest(
                                        OPPORTUNITY_ID
                                )
                        )
                );

        assertEquals(
                "Opportunity does not exist: "
                        + OPPORTUNITY_ID,
                exception.getMessage()
        );

        verifyNoInteractions(paperBetRepository);
    }

    private static PaperBetCandidateRecord qualifiedCandidate() {
        return candidate(
                "QUALIFIED",
                "APPROVED_MANUAL",
                "EXACT"
        );
    }

    private static PaperBetCandidateRecord candidate(
            String qualificationStatus,
            String mappingStatus,
            String settlementCompatibility
    ) {
        return new PaperBetCandidateRecord(
                OPPORTUNITY_ID,
                STRATEGY_VERSION_ID,
                MARKET_MAPPING_ID,
                SPORTING_EVENT_ID,
                SPORTSBOOK_MARKET_ID,
                SPORTSBOOK_OUTCOME_ID,
                PREDICTION_OUTCOME_ID,
                "test_book",
                Instant.parse("2026-08-01T10:00:00Z"),
                new BigDecimal("1.90"),
                new BigDecimal("0.58"),
                new BigDecimal("0.102"),
                qualificationStatus,
                mappingStatus,
                settlementCompatibility
        );
    }
}