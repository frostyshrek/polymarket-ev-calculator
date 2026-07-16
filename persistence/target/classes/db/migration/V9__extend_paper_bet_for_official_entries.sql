ALTER TABLE ufc_study.paper_bet
    RENAME COLUMN stake TO stake_units;

ALTER TABLE ufc_study.paper_bet
    RENAME COLUMN odds_taken TO decimal_odds;

ALTER TABLE ufc_study.paper_bet
    RENAME COLUMN probability_at_decision
        TO reference_probability;

ALTER TABLE ufc_study.paper_bet
    RENAME COLUMN estimated_ev_at_decision
        TO estimated_ev;

ALTER TABLE ufc_study.paper_bet
    RENAME COLUMN profit_loss
        TO net_profit_units;

ALTER TABLE ufc_study.paper_bet
    ALTER COLUMN simulation_account_id DROP NOT NULL,
    ALTER COLUMN expected_profit DROP NOT NULL,
    ALTER COLUMN bankroll_before DROP NOT NULL;

ALTER TABLE ufc_study.paper_bet
    ADD COLUMN strategy_version_id UUID,
    ADD COLUMN sporting_event_id UUID,
    ADD COLUMN market_mapping_id UUID,
    ADD COLUMN sportsbook_market_id UUID,
    ADD COLUMN sportsbook_outcome_id UUID,
    ADD COLUMN prediction_market_outcome_id UUID,
    ADD COLUMN bookmaker_code VARCHAR(100),
    ADD COLUMN stake_method VARCHAR(50),
    ADD COLUMN bet_result VARCHAR(30),
    ADD COLUMN gross_return_units NUMERIC(24, 8);


ALTER TABLE ufc_study.paper_bet
    ADD CONSTRAINT fk_paper_bet_strategy
        FOREIGN KEY (strategy_version_id)
        REFERENCES ufc_study.strategy_version (id),

    ADD CONSTRAINT fk_paper_bet_event
        FOREIGN KEY (sporting_event_id)
        REFERENCES ufc_study.sporting_event (id),

    ADD CONSTRAINT fk_paper_bet_mapping
        FOREIGN KEY (market_mapping_id)
        REFERENCES ufc_study.market_mapping (id),

    ADD CONSTRAINT fk_paper_bet_sportsbook_market
        FOREIGN KEY (sportsbook_market_id)
        REFERENCES ufc_study.source_market (id),

    ADD CONSTRAINT fk_paper_bet_sportsbook_outcome
        FOREIGN KEY (sportsbook_outcome_id)
        REFERENCES ufc_study.source_market_outcome (id),

    ADD CONSTRAINT fk_paper_bet_prediction_outcome
        FOREIGN KEY (prediction_market_outcome_id)
        REFERENCES ufc_study.source_market_outcome (id);



ALTER TABLE ufc_study.paper_bet
    DROP CONSTRAINT ck_paper_bet_probability,
    DROP CONSTRAINT ck_paper_bet_bankroll,
    DROP CONSTRAINT ck_paper_bet_status,
    DROP CONSTRAINT ck_paper_bet_settlement;


ALTER TABLE ufc_study.paper_bet
    ADD CONSTRAINT ck_paper_bet_probability
        CHECK (
            reference_probability > 0
            AND reference_probability < 1
        ),

    ADD CONSTRAINT ck_paper_bet_bankroll
        CHECK (
            (
                bankroll_before IS NULL
                AND bankroll_after IS NULL
            )
            OR
            (
                bankroll_before >= 0
                AND (
                    bankroll_after IS NULL
                    OR bankroll_after >= 0
                )
            )
        ),

    ADD CONSTRAINT ck_paper_bet_status
        CHECK (
            bet_status IN (
                'OPEN',
                'SETTLED'
            )
        ),

    ADD CONSTRAINT ck_paper_bet_result
        CHECK (
            bet_result IS NULL
            OR bet_result IN (
                'WIN',
                'LOSS',
                'VOID'
            )
        ),

    ADD CONSTRAINT ck_paper_bet_stake_method
        CHECK (
            stake_method IN (
                'FLAT_ONE_UNIT',
                'QUARTER_KELLY'
            )
        ),

    ADD CONSTRAINT ck_paper_bet_open_state
        CHECK (
            (
                bet_status = 'OPEN'
                AND bet_result IS NULL
                AND settled_at IS NULL
                AND gross_return_units IS NULL
                AND net_profit_units IS NULL
            )
            OR
            (
                bet_status = 'SETTLED'
                AND bet_result IS NOT NULL
                AND settled_at IS NOT NULL
                AND gross_return_units IS NOT NULL
                AND net_profit_units IS NOT NULL
            )
        );



ALTER TABLE ufc_study.paper_bet
    ADD CONSTRAINT uq_paper_bet_opportunity
        UNIQUE (opportunity_id);



ALTER TABLE ufc_study.paper_bet
    ADD CONSTRAINT uq_paper_bet_official_entry
        UNIQUE (
            strategy_version_id,
            sporting_event_id,
            sportsbook_outcome_id,
            bookmaker_code,
            stake_method
        );



ALTER TABLE ufc_study.paper_bet
    ALTER COLUMN strategy_version_id SET NOT NULL,
    ALTER COLUMN sporting_event_id SET NOT NULL,
    ALTER COLUMN market_mapping_id SET NOT NULL,
    ALTER COLUMN sportsbook_market_id SET NOT NULL,
    ALTER COLUMN sportsbook_outcome_id SET NOT NULL,
    ALTER COLUMN prediction_market_outcome_id SET NOT NULL,
    ALTER COLUMN bookmaker_code SET NOT NULL,
    ALTER COLUMN stake_method SET NOT NULL;



CREATE INDEX ix_paper_bet_event
    ON ufc_study.paper_bet (sporting_event_id);

CREATE INDEX ix_paper_bet_bookmaker
    ON ufc_study.paper_bet (bookmaker_code);

CREATE INDEX ix_paper_bet_placed_at
    ON ufc_study.paper_bet (placed_at);