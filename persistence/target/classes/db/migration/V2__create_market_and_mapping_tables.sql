CREATE TABLE ufc_study.source_market (
    id                      UUID PRIMARY KEY,
    data_source_id          UUID NOT NULL,
    source_event_reference_id UUID,
    external_market_id      VARCHAR(400) NOT NULL,
    market_type             VARCHAR(60) NOT NULL,
    market_name             VARCHAR(500),
    market_status           VARCHAR(40) NOT NULL DEFAULT 'OPEN',
    is_live                 BOOLEAN NOT NULL DEFAULT FALSE,
    rules_text              TEXT,
    rules_url               TEXT,
    resolution_source       TEXT,
    opens_at                TIMESTAMPTZ,
    closes_at               TIMESTAMPTZ,
    resolved_at             TIMESTAMPTZ,
    source_metadata         JSONB NOT NULL DEFAULT '{}'::JSONB,
    first_seen_at           TIMESTAMPTZ NOT NULL,
    last_seen_at            TIMESTAMPTZ NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_source_market_source
        FOREIGN KEY (data_source_id)
        REFERENCES ufc_study.data_source (id),

    CONSTRAINT fk_source_market_event_reference
        FOREIGN KEY (source_event_reference_id)
        REFERENCES ufc_study.source_event_reference (id),

    CONSTRAINT uq_source_external_market
        UNIQUE (data_source_id, external_market_id),

    CONSTRAINT ck_source_market_type
        CHECK (
            market_type IN (
                'MONEYLINE',
                'METHOD_OF_VICTORY',
                'TOTAL_ROUNDS',
                'GOES_DISTANCE',
                'ROUND_BETTING',
                'OTHER'
            )
        ),

    CONSTRAINT ck_source_market_status
        CHECK (
            market_status IN (
                'OPEN',
                'SUSPENDED',
                'CLOSED',
                'RESOLVED',
                'CANCELLED',
                'UNKNOWN'
            )
        ),

    CONSTRAINT ck_source_market_seen_order
        CHECK (last_seen_at >= first_seen_at)
);

CREATE TABLE ufc_study.source_market_outcome (
    id                      UUID PRIMARY KEY,
    source_market_id        UUID NOT NULL,
    participant_id          UUID,
    external_outcome_id     VARCHAR(400) NOT NULL,
    outcome_name            VARCHAR(300) NOT NULL,
    normalized_outcome_name VARCHAR(300) NOT NULL,
    outcome_type            VARCHAR(40) NOT NULL,
    display_order           SMALLINT,
    source_metadata         JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_source_market_outcome_market
        FOREIGN KEY (source_market_id)
        REFERENCES ufc_study.source_market (id),

    CONSTRAINT fk_source_market_outcome_participant
        FOREIGN KEY (participant_id)
        REFERENCES ufc_study.participant (id),

    CONSTRAINT uq_source_market_outcome
        UNIQUE (source_market_id, external_outcome_id),

    CONSTRAINT ck_source_market_outcome_type
        CHECK (
            outcome_type IN (
                'PARTICIPANT_WIN',
                'DRAW',
                'YES',
                'NO',
                'OTHER'
            )
        )
);

CREATE TABLE ufc_study.market_mapping (
    id                              UUID PRIMARY KEY,
    sporting_event_id               UUID NOT NULL,
    sportsbook_market_id            UUID NOT NULL,
    prediction_market_id            UUID NOT NULL,
    sportsbook_outcome_id           UUID NOT NULL,
    prediction_market_outcome_id    UUID NOT NULL,
    mapping_status                  VARCHAR(40) NOT NULL,
    settlement_compatibility        VARCHAR(40) NOT NULL,
    match_confidence                NUMERIC(8, 7),
    manually_approved               BOOLEAN NOT NULL DEFAULT FALSE,
    approval_notes                  TEXT,
    approved_by                     VARCHAR(200),
    approved_at                     TIMESTAMPTZ,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_market_mapping_event
        FOREIGN KEY (sporting_event_id)
        REFERENCES ufc_study.sporting_event (id),

    CONSTRAINT fk_market_mapping_sportsbook_market
        FOREIGN KEY (sportsbook_market_id)
        REFERENCES ufc_study.source_market (id),

    CONSTRAINT fk_market_mapping_prediction_market
        FOREIGN KEY (prediction_market_id)
        REFERENCES ufc_study.source_market (id),

    CONSTRAINT fk_market_mapping_sportsbook_outcome
        FOREIGN KEY (sportsbook_outcome_id)
        REFERENCES ufc_study.source_market_outcome (id),

    CONSTRAINT fk_market_mapping_prediction_outcome
        FOREIGN KEY (prediction_market_outcome_id)
        REFERENCES ufc_study.source_market_outcome (id),

    CONSTRAINT uq_market_mapping_pair
        UNIQUE (
            sportsbook_market_id,
            prediction_market_id,
            sportsbook_outcome_id,
            prediction_market_outcome_id
        ),

    CONSTRAINT ck_market_mapping_status
        CHECK (
            mapping_status IN (
                'APPROVED_AUTOMATIC',
                'APPROVED_MANUAL',
                'PENDING_REVIEW',
                'REJECTED',
                'AMBIGUOUS'
            )
        ),

    CONSTRAINT ck_settlement_compatibility
        CHECK (
            settlement_compatibility IN (
                'EXACT',
                'COMPATIBLE_WITH_ADJUSTMENT',
                'AMBIGUOUS',
                'INCOMPATIBLE'
            )
        ),

    CONSTRAINT ck_match_confidence
        CHECK (
            match_confidence IS NULL
            OR (
                match_confidence >= 0
                AND match_confidence <= 1
            )
        ),

    CONSTRAINT ck_market_mapping_approval
        CHECK (
            manually_approved = FALSE
            OR approved_at IS NOT NULL
        )
);