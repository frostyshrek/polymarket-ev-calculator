ALTER TABLE ufc_study.opportunity
    ADD COLUMN opposing_sportsbook_snapshot_id UUID;

ALTER TABLE ufc_study.opportunity
    ADD CONSTRAINT fk_opportunity_opposing_sportsbook_snapshot
        FOREIGN KEY (opposing_sportsbook_snapshot_id)
        REFERENCES ufc_study.sportsbook_odds_snapshot (id);

CREATE INDEX ix_opportunity_opposing_snapshot
    ON ufc_study.opportunity (
        opposing_sportsbook_snapshot_id
    );

INSERT INTO ufc_study.strategy_version (
    id,
    strategy_code,
    version_number,
    description,
    strategy_status,
    effective_from,
    probability_method,
    minimum_ev,
    maximum_market_spread,
    maximum_snapshot_age_seconds,
    maximum_source_gap_seconds,
    minimum_prefight_seconds,
    duplicate_entry_rule,
    configuration,
    specification_hash
)
VALUES (
    '40000000-0000-0000-0000-000000000001',
    'UFC_EV',
    '1.0',
    'Initial preregistered UFC Polymarket EV strategy',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    'BEST_BID',
    0.05,
    0.04,
    60,
    60,
    1800,
    'FIRST_QUALIFYING_PER_FIGHTER_BOOKMAKER_FIGHT',
    '{
      "sport": "MMA",
      "competition": "UFC",
      "marketType": "MONEYLINE",
      "liveAllowed": false,
      "settlementCompatibility": "EXACT",
      "stakingPrimary": "FLAT_ONE_UNIT"
    }'::JSONB,
    'ufc-ev-v1.0-best-bid-ev005-spread004-age60-gap60-prefight1800'
)
ON CONFLICT (strategy_code, version_number) DO NOTHING;