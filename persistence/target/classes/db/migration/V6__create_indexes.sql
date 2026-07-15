CREATE INDEX ix_sporting_event_start_time
    ON ufc_study.sporting_event (scheduled_start_time);

CREATE INDEX ix_sporting_event_status_start
    ON ufc_study.sporting_event (
        event_status,
        scheduled_start_time
    );

CREATE INDEX ix_participant_normalized_name
    ON ufc_study.participant (normalized_name);

CREATE INDEX ix_participant_alias_normalized
    ON ufc_study.participant_alias (normalized_alias);

CREATE INDEX ix_source_market_event
    ON ufc_study.source_market (source_event_reference_id);

CREATE INDEX ix_source_market_type_status
    ON ufc_study.source_market (
        market_type,
        market_status
    );

CREATE INDEX ix_market_mapping_event_status
    ON ufc_study.market_mapping (
        sporting_event_id,
        mapping_status
    );

CREATE INDEX ix_ingestion_run_source_started
    ON ufc_study.ingestion_run (
        data_source_id,
        started_at DESC
    );

CREATE INDEX ix_raw_payload_source_received
    ON ufc_study.raw_source_payload (
        data_source_id,
        received_at DESC
    );

CREATE INDEX ix_raw_payload_hash
    ON ufc_study.raw_source_payload (payload_hash);

CREATE INDEX ix_sportsbook_snapshot_market_observed
    ON ufc_study.sportsbook_odds_snapshot (
        source_market_id,
        observed_at DESC
    );

CREATE INDEX ix_sportsbook_snapshot_outcome_observed
    ON ufc_study.sportsbook_odds_snapshot (
        source_outcome_id,
        observed_at DESC
    );

CREATE INDEX ix_sportsbook_snapshot_bookmaker_observed
    ON ufc_study.sportsbook_odds_snapshot (
        bookmaker_code,
        observed_at DESC
    );

CREATE INDEX ix_prediction_snapshot_market_observed
    ON ufc_study.prediction_market_snapshot (
        source_market_id,
        observed_at DESC
    );

CREATE INDEX ix_prediction_snapshot_outcome_observed
    ON ufc_study.prediction_market_snapshot (
        source_outcome_id,
        observed_at DESC
    );

CREATE INDEX ix_opportunity_strategy_decision
    ON ufc_study.opportunity (
        strategy_version_id,
        decision_time DESC
    );

CREATE INDEX ix_opportunity_status_decision
    ON ufc_study.opportunity (
        qualification_status,
        decision_time DESC
    );

CREATE INDEX ix_paper_bet_account_placed
    ON ufc_study.paper_bet (
        simulation_account_id,
        placed_at
    );

CREATE INDEX ix_paper_bet_status
    ON ufc_study.paper_bet (bet_status);

CREATE INDEX ix_event_resolution_event_observed
    ON ufc_study.event_resolution (
        sporting_event_id,
        observed_at DESC
    );

CREATE INDEX ix_audit_record_entity
    ON ufc_study.audit_record (
        entity_type,
        entity_id,
        occurred_at DESC
    );

CREATE INDEX ix_raw_payload_json
    ON ufc_study.raw_source_payload
    USING GIN (payload);

CREATE INDEX ix_participant_metadata_json
    ON ufc_study.participant
    USING GIN (external_metadata);