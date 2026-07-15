CREATE SCHEMA IF NOT EXISTS ufc_study;

CREATE TABLE ufc_study.data_source (
    id                  UUID PRIMARY KEY,
    source_code         VARCHAR(50) NOT NULL,
    display_name        VARCHAR(150) NOT NULL,
    source_type         VARCHAR(40) NOT NULL,
    base_url            TEXT,
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_data_source_code
        UNIQUE (source_code),

    CONSTRAINT ck_data_source_type
        CHECK (
            source_type IN (
                'SPORTSBOOK_ODDS_PROVIDER',
                'PREDICTION_MARKET',
                'RESULT_PROVIDER',
                'MANUAL'
            )
        )
);

CREATE TABLE ufc_study.sport (
    id                  UUID PRIMARY KEY,
    sport_code          VARCHAR(50) NOT NULL,
    display_name        VARCHAR(100) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_sport_code
        UNIQUE (sport_code)
);

CREATE TABLE ufc_study.competition (
    id                  UUID PRIMARY KEY,
    sport_id            UUID NOT NULL,
    competition_code    VARCHAR(100) NOT NULL,
    display_name        VARCHAR(200) NOT NULL,
    country_code        VARCHAR(3),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_competition_sport
        FOREIGN KEY (sport_id)
        REFERENCES ufc_study.sport (id),

    CONSTRAINT uq_competition_code
        UNIQUE (sport_id, competition_code)
);

CREATE TABLE ufc_study.participant (
    id                  UUID PRIMARY KEY,
    sport_id            UUID NOT NULL,
    canonical_name      VARCHAR(200) NOT NULL,
    normalized_name     VARCHAR(200) NOT NULL,
    external_metadata   JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_participant_sport
        FOREIGN KEY (sport_id)
        REFERENCES ufc_study.sport (id),

    CONSTRAINT uq_participant_normalized_name
        UNIQUE (sport_id, normalized_name)
);

CREATE TABLE ufc_study.participant_alias (
    id                  UUID PRIMARY KEY,
    participant_id      UUID NOT NULL,
    data_source_id      UUID,
    alias_name          VARCHAR(200) NOT NULL,
    normalized_alias    VARCHAR(200) NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_participant_alias_participant
        FOREIGN KEY (participant_id)
        REFERENCES ufc_study.participant (id),

    CONSTRAINT fk_participant_alias_source
        FOREIGN KEY (data_source_id)
        REFERENCES ufc_study.data_source (id),

    CONSTRAINT uq_participant_alias
        UNIQUE (
            participant_id,
            data_source_id,
            normalized_alias
        )
);

CREATE TABLE ufc_study.sporting_event (
    id                      UUID PRIMARY KEY,
    sport_id                UUID NOT NULL,
    competition_id          UUID,
    canonical_name          VARCHAR(300) NOT NULL,
    scheduled_start_time    TIMESTAMPTZ NOT NULL,
    actual_start_time       TIMESTAMPTZ,
    event_status            VARCHAR(40) NOT NULL DEFAULT 'SCHEDULED',
    venue                   VARCHAR(250),
    source_metadata         JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sporting_event_sport
        FOREIGN KEY (sport_id)
        REFERENCES ufc_study.sport (id),

    CONSTRAINT fk_sporting_event_competition
        FOREIGN KEY (competition_id)
        REFERENCES ufc_study.competition (id),

    CONSTRAINT ck_sporting_event_status
        CHECK (
            event_status IN (
                'SCHEDULED',
                'POSTPONED',
                'STARTED',
                'COMPLETED',
                'CANCELLED',
                'ABANDONED'
            )
        )
);

CREATE TABLE ufc_study.event_participant (
    event_id             UUID NOT NULL,
    participant_id       UUID NOT NULL,
    participant_role     VARCHAR(30) NOT NULL,
    display_order        SMALLINT NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (event_id, participant_id),

    CONSTRAINT fk_event_participant_event
        FOREIGN KEY (event_id)
        REFERENCES ufc_study.sporting_event (id),

    CONSTRAINT fk_event_participant_participant
        FOREIGN KEY (participant_id)
        REFERENCES ufc_study.participant (id),

    CONSTRAINT uq_event_display_order
        UNIQUE (event_id, display_order),

    CONSTRAINT ck_event_participant_role
        CHECK (
            participant_role IN (
                'COMPETITOR',
                'HOME',
                'AWAY'
            )
        ),

    CONSTRAINT ck_event_display_order
        CHECK (display_order > 0)
);

CREATE TABLE ufc_study.source_event_reference (
    id                  UUID PRIMARY KEY,
    sporting_event_id   UUID NOT NULL,
    data_source_id      UUID NOT NULL,
    external_event_id   VARCHAR(300) NOT NULL,
    external_event_name VARCHAR(500),
    first_seen_at       TIMESTAMPTZ NOT NULL,
    last_seen_at        TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_source_event_reference_event
        FOREIGN KEY (sporting_event_id)
        REFERENCES ufc_study.sporting_event (id),

    CONSTRAINT fk_source_event_reference_source
        FOREIGN KEY (data_source_id)
        REFERENCES ufc_study.data_source (id),

    CONSTRAINT uq_source_external_event
        UNIQUE (data_source_id, external_event_id),

    CONSTRAINT ck_source_event_seen_order
        CHECK (last_seen_at >= first_seen_at)
);