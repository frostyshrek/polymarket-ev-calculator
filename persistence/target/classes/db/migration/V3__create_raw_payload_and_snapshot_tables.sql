CREATE TABLE ufc_study.ingestion_run (
    id                  UUID PRIMARY KEY,
    data_source_id      UUID NOT NULL,
    ingestion_type      VARCHAR(60) NOT NULL,
    started_at          TIMESTAMPTZ NOT NULL,
    completed_at        TIMESTAMPTZ,
    run_status          VARCHAR(30) NOT NULL,
    records_received    INTEGER NOT NULL DEFAULT 0,
    records_persisted   INTEGER NOT NULL DEFAULT 0,
    error_message       TEXT,
    metadata            JSONB NOT NULL DEFAULT '{}'::JSONB,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ingestion_run_source
        FOREIGN KEY (data_source_id)
        REFERENCES ufc_study.data_source (id),

    CONSTRAINT ck_ingestion_run_type
        CHECK (
            ingestion_type IN (
                'EVENT_DISCOVERY',
                'MARKET_DISCOVERY',
                'SPORTSBOOK_ODDS',
                'PREDICTION_MARKET_BOOK',
                'RESULTS',
                'CLOSING_ODDS'
            )
        ),

    CONSTRAINT ck_ingestion_run_status
        CHECK (
            run_status IN (
                'STARTED',
                'SUCCEEDED',
                'PARTIALLY_SUCCEEDED',
                'FAILED'
            )
        ),

    CONSTRAINT ck_ingestion_run_counts
        CHECK (
            records_received >= 0
            AND records_persisted >= 0
        ),

    CONSTRAINT ck_ingestion_run_times
        CHECK (
            completed_at IS NULL
            OR completed_at >= started_at
        )
);

CREATE TABLE ufc_study.raw_source_payload (
    id                  UUID PRIMARY KEY,
    ingestion_run_id    UUID NOT NULL,
    data_source_id      UUID NOT NULL,
    endpoint_name       VARCHAR(200) NOT NULL,
    request_uri         TEXT,
    external_reference VARCHAR(500),
    response_status     INTEGER,
    payload             JSONB NOT NULL,
    payload_hash        VARCHAR(128) NOT NULL,
    source_timestamp    TIMESTAMPTZ,
    received_at         TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_raw_payload_ingestion_run
        FOREIGN KEY (ingestion_run_id)
        REFERENCES ufc_study.ingestion_run (id),

    CONSTRAINT fk_raw_payload_source
        FOREIGN KEY (data_source_id)
        REFERENCES ufc_study.data_source (id),

    CONSTRAINT ck_raw_payload_response_status
        CHECK (
            response_status IS NULL
            OR (
                response_status >= 100
                AND response_status <= 599
            )
        )
);

CREATE TABLE ufc_study.sportsbook_odds_snapshot (
    id                      UUID PRIMARY KEY,
    ingestion_run_id        UUID NOT NULL,
    raw_payload_id          UUID NOT NULL,
    source_market_id        UUID NOT NULL,
    source_outcome_id       UUID NOT NULL,
    bookmaker_code          VARCHAR(100) NOT NULL,
    decimal_odds            NUMERIC(20, 10) NOT NULL,
    is_live                 BOOLEAN NOT NULL DEFAULT FALSE,
    source_updated_at       TIMESTAMPTZ,
    observed_at             TIMESTAMPTZ NOT NULL,
    maximum_stake           NUMERIC(20, 6),
    market_suspended        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sportsbook_snapshot_run
        FOREIGN KEY (ingestion_run_id)
        REFERENCES ufc_study.ingestion_run (id),

    CONSTRAINT fk_sportsbook_snapshot_raw_payload
        FOREIGN KEY (raw_payload_id)
        REFERENCES ufc_study.raw_source_payload (id),

    CONSTRAINT fk_sportsbook_snapshot_market
        FOREIGN KEY (source_market_id)
        REFERENCES ufc_study.source_market (id),

    CONSTRAINT fk_sportsbook_snapshot_outcome
        FOREIGN KEY (source_outcome_id)
        REFERENCES ufc_study.source_market_outcome (id),

    CONSTRAINT ck_sportsbook_decimal_odds
        CHECK (decimal_odds > 1),

    CONSTRAINT ck_sportsbook_maximum_stake
        CHECK (
            maximum_stake IS NULL
            OR maximum_stake >= 0
        )
);

CREATE TABLE ufc_study.prediction_market_snapshot (
    id                      UUID PRIMARY KEY,
    ingestion_run_id        UUID NOT NULL,
    raw_payload_id          UUID NOT NULL,
    source_market_id        UUID NOT NULL,
    source_outcome_id       UUID NOT NULL,
    best_bid                NUMERIC(20, 10) NOT NULL,
    best_ask                NUMERIC(20, 10) NOT NULL,
    midpoint                NUMERIC(20, 10) NOT NULL,
    spread                  NUMERIC(20, 10) NOT NULL,
    last_trade_price        NUMERIC(20, 10),
    bid_depth               NUMERIC(24, 8),
    ask_depth               NUMERIC(24, 8),
    market_volume           NUMERIC(24, 8),
    market_liquidity        NUMERIC(24, 8),
    source_updated_at       TIMESTAMPTZ,
    observed_at             TIMESTAMPTZ NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prediction_snapshot_run
        FOREIGN KEY (ingestion_run_id)
        REFERENCES ufc_study.ingestion_run (id),

    CONSTRAINT fk_prediction_snapshot_raw_payload
        FOREIGN KEY (raw_payload_id)
        REFERENCES ufc_study.raw_source_payload (id),

    CONSTRAINT fk_prediction_snapshot_market
        FOREIGN KEY (source_market_id)
        REFERENCES ufc_study.source_market (id),

    CONSTRAINT fk_prediction_snapshot_outcome
        FOREIGN KEY (source_outcome_id)
        REFERENCES ufc_study.source_market_outcome (id),

    CONSTRAINT ck_prediction_best_bid
        CHECK (best_bid >= 0 AND best_bid <= 1),

    CONSTRAINT ck_prediction_best_ask
        CHECK (best_ask >= 0 AND best_ask <= 1),

    CONSTRAINT ck_prediction_midpoint
        CHECK (midpoint >= 0 AND midpoint <= 1),

    CONSTRAINT ck_prediction_last_trade
        CHECK (
            last_trade_price IS NULL
            OR (
                last_trade_price >= 0
                AND last_trade_price <= 1
            )
        ),

    CONSTRAINT ck_prediction_quote_order
        CHECK (best_bid <= best_ask),

    CONSTRAINT ck_prediction_spread
        CHECK (
            spread >= 0
            AND spread <= 1
            AND spread = best_ask - best_bid
        ),

    CONSTRAINT ck_prediction_midpoint_calculation
        CHECK (
            midpoint = (best_bid + best_ask) / 2
        ),

    CONSTRAINT ck_prediction_depth
        CHECK (
            (bid_depth IS NULL OR bid_depth >= 0)
            AND (ask_depth IS NULL OR ask_depth >= 0)
            AND (market_volume IS NULL OR market_volume >= 0)
            AND (market_liquidity IS NULL OR market_liquidity >= 0)
        )
);