CREATE TABLE ufc_study.simulation_account (
    id                  UUID PRIMARY KEY,
    strategy_version_id UUID NOT NULL,
    account_code        VARCHAR(100) NOT NULL,
    staking_method      VARCHAR(50) NOT NULL,
    initial_bankroll    NUMERIC(24, 8) NOT NULL,
    current_bankroll    NUMERIC(24, 8) NOT NULL,
    account_status      VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_simulation_account_strategy
        FOREIGN KEY (strategy_version_id)
        REFERENCES ufc_study.strategy_version (id),

    CONSTRAINT uq_simulation_account
        UNIQUE (strategy_version_id, account_code),

    CONSTRAINT ck_simulation_staking_method
        CHECK (
            staking_method IN (
                'FLAT_ONE_UNIT',
                'FIXED_BANKROLL_PERCENTAGE',
                'FULL_KELLY',
                'HALF_KELLY',
                'QUARTER_KELLY',
                'CAPPED_QUARTER_KELLY'
            )
        ),

    CONSTRAINT ck_simulation_bankrolls
        CHECK (
            initial_bankroll > 0
            AND current_bankroll >= 0
        ),

    CONSTRAINT ck_simulation_account_status
        CHECK (
            account_status IN (
                'ACTIVE',
                'CLOSED',
                'BANKRUPT'
            )
        )
);

CREATE TABLE ufc_study.paper_bet (
    id                      UUID PRIMARY KEY,
    opportunity_id          UUID NOT NULL,
    simulation_account_id   UUID NOT NULL,
    stake                   NUMERIC(24, 8) NOT NULL,
    odds_taken              NUMERIC(20, 10) NOT NULL,
    probability_at_decision NUMERIC(20, 10) NOT NULL,
    estimated_ev_at_decision NUMERIC(20, 10) NOT NULL,
    expected_profit         NUMERIC(24, 8) NOT NULL,
    bankroll_before         NUMERIC(24, 8) NOT NULL,
    bankroll_after          NUMERIC(24, 8),
    bet_status              VARCHAR(40) NOT NULL,
    placed_at               TIMESTAMPTZ NOT NULL,
    settled_at              TIMESTAMPTZ,
    profit_loss             NUMERIC(24, 8),
    exclusion_code          VARCHAR(80),
    exclusion_reason        TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_paper_bet_opportunity
        FOREIGN KEY (opportunity_id)
        REFERENCES ufc_study.opportunity (id),

    CONSTRAINT fk_paper_bet_account
        FOREIGN KEY (simulation_account_id)
        REFERENCES ufc_study.simulation_account (id),

    CONSTRAINT uq_paper_bet_account_opportunity
        UNIQUE (simulation_account_id, opportunity_id),

    CONSTRAINT ck_paper_bet_stake
        CHECK (stake > 0),

    CONSTRAINT ck_paper_bet_odds
        CHECK (odds_taken > 1),

    CONSTRAINT ck_paper_bet_probability
        CHECK (
            probability_at_decision >= 0
            AND probability_at_decision <= 1
        ),

    CONSTRAINT ck_paper_bet_bankroll
        CHECK (
            bankroll_before >= 0
            AND (
                bankroll_after IS NULL
                OR bankroll_after >= 0
            )
        ),

    CONSTRAINT ck_paper_bet_status
        CHECK (
            bet_status IN (
                'OPEN',
                'WON',
                'LOST',
                'VOID_CANCELLED',
                'VOID_POSTPONED',
                'VOID_DRAW',
                'VOID_NO_CONTEST',
                'EXCLUDED'
            )
        ),

    CONSTRAINT ck_paper_bet_settlement
        CHECK (
            (
                bet_status = 'OPEN'
                AND settled_at IS NULL
                AND profit_loss IS NULL
                AND bankroll_after IS NULL
            )
            OR
            (
                bet_status <> 'OPEN'
                AND settled_at IS NOT NULL
                AND profit_loss IS NOT NULL
                AND bankroll_after IS NOT NULL
            )
        ),

    CONSTRAINT ck_paper_bet_exclusion
        CHECK (
            bet_status <> 'EXCLUDED'
            OR (
                exclusion_code IS NOT NULL
                AND exclusion_reason IS NOT NULL
            )
        )
);

CREATE TABLE ufc_study.event_resolution (
    id                      UUID PRIMARY KEY,
    sporting_event_id       UUID NOT NULL,
    winning_participant_id  UUID,
    official_result_type    VARCHAR(40) NOT NULL,
    official_result_text    VARCHAR(500),
    result_source_id        UUID,
    source_external_result_id VARCHAR(300),
    official_result_at      TIMESTAMPTZ,
    observed_at             TIMESTAMPTZ NOT NULL,
    is_final                BOOLEAN NOT NULL DEFAULT FALSE,
    metadata                JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_event_resolution_event
        FOREIGN KEY (sporting_event_id)
        REFERENCES ufc_study.sporting_event (id),

    CONSTRAINT fk_event_resolution_winner
        FOREIGN KEY (winning_participant_id)
        REFERENCES ufc_study.participant (id),

    CONSTRAINT fk_event_resolution_source
        FOREIGN KEY (result_source_id)
        REFERENCES ufc_study.data_source (id),

    CONSTRAINT ck_event_resolution_type
        CHECK (
            official_result_type IN (
                'PARTICIPANT_WIN',
                'DRAW',
                'NO_CONTEST',
                'CANCELLED',
                'POSTPONED',
                'UNKNOWN'
            )
        ),

    CONSTRAINT ck_event_resolution_winner
        CHECK (
            (
                official_result_type = 'PARTICIPANT_WIN'
                AND winning_participant_id IS NOT NULL
            )
            OR
            (
                official_result_type <> 'PARTICIPANT_WIN'
                AND winning_participant_id IS NULL
            )
        )
);

CREATE TABLE ufc_study.closing_odds (
    id                      UUID PRIMARY KEY,
    paper_bet_id            UUID NOT NULL,
    sportsbook_snapshot_id  UUID NOT NULL,
    closing_decimal_odds    NUMERIC(20, 10) NOT NULL,
    closing_raw_probability NUMERIC(20, 10) NOT NULL,
    closing_no_vig_probability NUMERIC(20, 10),
    odds_based_clv          NUMERIC(20, 10),
    probability_based_clv   NUMERIC(20, 10),
    observed_at             TIMESTAMPTZ NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_closing_odds_bet
        FOREIGN KEY (paper_bet_id)
        REFERENCES ufc_study.paper_bet (id),

    CONSTRAINT fk_closing_odds_snapshot
        FOREIGN KEY (sportsbook_snapshot_id)
        REFERENCES ufc_study.sportsbook_odds_snapshot (id),

    CONSTRAINT uq_closing_odds_bet
        UNIQUE (paper_bet_id),

    CONSTRAINT ck_closing_odds_decimal
        CHECK (closing_decimal_odds > 1),

    CONSTRAINT ck_closing_odds_probability
        CHECK (
            closing_raw_probability > 0
            AND closing_raw_probability < 1
        ),

    CONSTRAINT ck_closing_odds_no_vig
        CHECK (
            closing_no_vig_probability IS NULL
            OR (
                closing_no_vig_probability >= 0
                AND closing_no_vig_probability <= 1
            )
        )
);

CREATE TABLE ufc_study.audit_record (
    id                  UUID PRIMARY KEY,
    entity_type         VARCHAR(80) NOT NULL,
    entity_id           UUID NOT NULL,
    action_type         VARCHAR(80) NOT NULL,
    reason_code         VARCHAR(100),
    description         TEXT NOT NULL,
    previous_state      JSONB,
    new_state           JSONB,
    performed_by        VARCHAR(200) NOT NULL,
    occurred_at         TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);