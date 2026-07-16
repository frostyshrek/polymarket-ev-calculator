INSERT INTO ufc_study.data_source (
    id,
    source_code,
    display_name,
    source_type,
    base_url,
    is_active
)
VALUES
(
    '10000000-0000-0000-0000-000000000001',
    'THE_ODDS_API',
    'The Odds API',
    'SPORTSBOOK_ODDS_PROVIDER',
    'https://api.the-odds-api.com',
    TRUE
),
(
    '10000000-0000-0000-0000-000000000002',
    'POLYMARKET_GAMMA',
    'Polymarket Gamma',
    'PREDICTION_MARKET',
    'https://gamma-api.polymarket.com',
    TRUE
),
(
    '10000000-0000-0000-0000-000000000003',
    'POLYMARKET_CLOB',
    'Polymarket CLOB',
    'PREDICTION_MARKET',
    'https://clob.polymarket.com',
    TRUE
)
ON CONFLICT (source_code) DO NOTHING;

CREATE TABLE ufc_study.ingestion_rejection (
    id                  UUID PRIMARY KEY,
    ingestion_run_id    UUID NOT NULL,
    raw_payload_id      UUID NOT NULL,
    data_source_id      UUID NOT NULL,
    external_reference VARCHAR(500),
    rejection_code      VARCHAR(100) NOT NULL,
    rejection_reason    TEXT NOT NULL,
    rejected_record     JSONB,
    occurred_at         TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ingestion_rejection_run
        FOREIGN KEY (ingestion_run_id)
        REFERENCES ufc_study.ingestion_run (id),

    CONSTRAINT fk_ingestion_rejection_payload
        FOREIGN KEY (raw_payload_id)
        REFERENCES ufc_study.raw_source_payload (id),

    CONSTRAINT fk_ingestion_rejection_source
        FOREIGN KEY (data_source_id)
        REFERENCES ufc_study.data_source (id)
);

CREATE INDEX ix_ingestion_rejection_run
    ON ufc_study.ingestion_rejection (
        ingestion_run_id,
        occurred_at
    );

CREATE INDEX ix_ingestion_rejection_code
    ON ufc_study.ingestion_rejection (
        rejection_code,
        occurred_at DESC
    );