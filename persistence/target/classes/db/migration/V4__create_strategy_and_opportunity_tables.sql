CREATE TABLE ufc_study.strategy_version (
    id                          UUID PRIMARY KEY,
    strategy_code               VARCHAR(100) NOT NULL,
    version_number              VARCHAR(30) NOT NULL,
    description                 TEXT NOT NULL,
    strategy_status             VARCHAR(30) NOT NULL,
    effective_from              TIMESTAMPTZ,
    effective_until             TIMESTAMPTZ,
    probability_method          VARCHAR(40) NOT NULL,
    minimum_ev                  NUMERIC(20, 10) NOT NULL,
    maximum_market_spread       NUMERIC(20, 10) NOT NULL,
    maximum_snapshot_age_seconds INTEGER NOT NULL,
    maximum_source_gap_seconds  INTEGER NOT NULL,
    minimum_prefight_seconds    INTEGER NOT NULL,
    duplicate_entry_rule        VARCHAR(80) NOT NULL,
    configuration               JSONB NOT NULL,
    specification_hash          VARCHAR(128) NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_strategy_version
        UNIQUE (strategy_code, version_number),

    CONSTRAINT uq_strategy_specification_hash
        UNIQUE (specification_hash),

    CONSTRAINT ck_strategy_status
        CHECK (
            strategy_status IN (
                'DRAFT',
                'ACTIVE',
                'CLOSED',
                'ARCHIVED'
            )
        ),

    CONSTRAINT ck_strategy_probability_method
        CHECK (
            probability_method IN (
                'BEST_BID',
                'MIDPOINT',
                'LAST_TRADE',
                'CALIBRATED',
                'ENSEMBLE'
            )
        ),

    CONSTRAINT ck_strategy_thresholds
        CHECK (
            minimum_ev >= 0
            AND maximum_market_spread >= 0
            AND maximum_market_spread <= 1
            AND maximum_snapshot_age_seconds > 0
            AND maximum_source_gap_seconds > 0
            AND minimum_prefight_seconds >= 0
        ),

    CONSTRAINT ck_strategy_effective_period
        CHECK (
            effective_until IS NULL
            OR effective_from IS NULL
            OR effective_until > effective_from
        )
);

CREATE TABLE ufc_study.opportunity (
    id                              UUID PRIMARY KEY,
    strategy_version_id             UUID NOT NULL,
    market_mapping_id               UUID NOT NULL,
    sportsbook_snapshot_id          UUID NOT NULL,
    prediction_snapshot_id          UUID NOT NULL,
    decision_time                   TIMESTAMPTZ NOT NULL,
    reference_probability           NUMERIC(20, 10) NOT NULL,
    probability_method              VARCHAR(40) NOT NULL,
    sportsbook_decimal_odds         NUMERIC(20, 10) NOT NULL,
    raw_implied_probability         NUMERIC(20, 10) NOT NULL,
    sportsbook_no_vig_probability   NUMERIC(20, 10),
    estimated_ev                    NUMERIC(20, 10) NOT NULL,
    expected_profit_per_unit        NUMERIC(20, 10) NOT NULL,
    prediction_market_spread        NUMERIC(20, 10) NOT NULL,
    sportsbook_snapshot_age_seconds INTEGER NOT NULL,
    prediction_snapshot_age_seconds INTEGER NOT NULL,
    source_gap_seconds              INTEGER NOT NULL,
    seconds_until_scheduled_start   INTEGER NOT NULL,
    qualification_status            VARCHAR(30) NOT NULL,
    qualification_reason            TEXT NOT NULL,
    rejection_code                  VARCHAR(80),
    calculation_version             VARCHAR(50) NOT NULL,
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_opportunity_strategy_version
        FOREIGN KEY (strategy_version_id)
        REFERENCES ufc_study.strategy_version (id),

    CONSTRAINT fk_opportunity_mapping
        FOREIGN KEY (market_mapping_id)
        REFERENCES ufc_study.market_mapping (id),

    CONSTRAINT fk_opportunity_sportsbook_snapshot
        FOREIGN KEY (sportsbook_snapshot_id)
        REFERENCES ufc_study.sportsbook_odds_snapshot (id),

    CONSTRAINT fk_opportunity_prediction_snapshot
        FOREIGN KEY (prediction_snapshot_id)
        REFERENCES ufc_study.prediction_market_snapshot (id),

    CONSTRAINT uq_opportunity_snapshot_pair_strategy
        UNIQUE (
            strategy_version_id,
            market_mapping_id,
            sportsbook_snapshot_id,
            prediction_snapshot_id
        ),

    CONSTRAINT ck_opportunity_probability
        CHECK (
            reference_probability >= 0
            AND reference_probability <= 1
        ),

    CONSTRAINT ck_opportunity_implied_probability
        CHECK (
            raw_implied_probability > 0
            AND raw_implied_probability < 1
        ),

    CONSTRAINT ck_opportunity_no_vig_probability
        CHECK (
            sportsbook_no_vig_probability IS NULL
            OR (
                sportsbook_no_vig_probability >= 0
                AND sportsbook_no_vig_probability <= 1
            )
        ),

    CONSTRAINT ck_opportunity_odds
        CHECK (sportsbook_decimal_odds > 1),

    CONSTRAINT ck_opportunity_spread
        CHECK (
            prediction_market_spread >= 0
            AND prediction_market_spread <= 1
        ),

    CONSTRAINT ck_opportunity_timing
        CHECK (
            sportsbook_snapshot_age_seconds >= 0
            AND prediction_snapshot_age_seconds >= 0
            AND source_gap_seconds >= 0
        ),

    CONSTRAINT ck_opportunity_status
        CHECK (
            qualification_status IN (
                'QUALIFIED',
                'REJECTED',
                'REQUIRES_REVIEW'
            )
        ),

    CONSTRAINT ck_opportunity_rejection_code
        CHECK (
            qualification_status <> 'REJECTED'
            OR rejection_code IS NOT NULL
        )
);