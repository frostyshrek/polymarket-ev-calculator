CREATE TABLE ufc_study.automated_match_run
(
    id                  UUID PRIMARY KEY,
    started_at          TIMESTAMPTZ NOT NULL,
    completed_at        TIMESTAMPTZ,
    run_status          VARCHAR(32) NOT NULL,
    candidates_created  INTEGER NOT NULL DEFAULT 0,
    matches_approved    INTEGER NOT NULL DEFAULT 0,
    review_required     INTEGER NOT NULL DEFAULT 0,
    error_message       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_automated_match_run_status
        CHECK (
            run_status IN (
                'RUNNING',
                'COMPLETED',
                'FAILED'
            )
        ),

    CONSTRAINT ck_automated_match_run_counts
        CHECK (
            candidates_created >= 0
            AND matches_approved >= 0
            AND review_required >= 0
        )
);

CREATE TABLE ufc_study.automated_match_candidate
(
    id                          UUID PRIMARY KEY,
    automated_match_run_id      UUID NOT NULL,
    sportsbook_market_id        UUID NOT NULL,
    prediction_market_id        UUID NOT NULL,

    participant_score           NUMERIC(8, 6) NOT NULL,
    scheduled_time_score        NUMERIC(8, 6) NOT NULL,
    overall_score               NUMERIC(8, 6) NOT NULL,

    scheduled_time_difference_seconds BIGINT,

    matching_status             VARCHAR(32) NOT NULL,
    decision_reason             VARCHAR(128) NOT NULL,

    sportsbook_participant_key  TEXT NOT NULL,
    prediction_participant_key  TEXT NOT NULL,

    created_mapping_id          UUID,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at                 TIMESTAMPTZ,
    reviewed_by                 VARCHAR(128),

    CONSTRAINT fk_automated_candidate_run
        FOREIGN KEY (automated_match_run_id)
        REFERENCES ufc_study.automated_match_run (id),

    CONSTRAINT fk_automated_candidate_sportsbook_market
        FOREIGN KEY (sportsbook_market_id)
        REFERENCES ufc_study.source_market (id),

    CONSTRAINT fk_automated_candidate_prediction_market
        FOREIGN KEY (prediction_market_id)
        REFERENCES ufc_study.source_market (id),

    CONSTRAINT fk_automated_candidate_created_mapping
        FOREIGN KEY (created_mapping_id)
        REFERENCES ufc_study.market_mapping (id),

    CONSTRAINT uq_automated_candidate_run_pair
        UNIQUE (
            automated_match_run_id,
            sportsbook_market_id,
            prediction_market_id
        ),

    CONSTRAINT ck_automated_candidate_scores
        CHECK (
            participant_score BETWEEN 0 AND 1
            AND scheduled_time_score BETWEEN 0 AND 1
            AND overall_score BETWEEN 0 AND 1
        ),

    CONSTRAINT ck_automated_candidate_status
        CHECK (
            matching_status IN (
                'AUTO_APPROVED',
                'REVIEW_REQUIRED',
                'REJECTED',
                'SUPERSEDED'
            )
        )
);

CREATE INDEX ix_automated_candidate_status_score
    ON ufc_study.automated_match_candidate
       (matching_status, overall_score DESC);

CREATE INDEX ix_automated_candidate_sportsbook_market
    ON ufc_study.automated_match_candidate
       (sportsbook_market_id);

CREATE INDEX ix_automated_candidate_prediction_market
    ON ufc_study.automated_match_candidate
       (prediction_market_id);