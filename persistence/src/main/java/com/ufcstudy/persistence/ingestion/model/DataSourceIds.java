package com.ufcstudy.persistence.ingestion.model;

import java.util.UUID;

public final class DataSourceIds {

    public static final UUID THE_ODDS_API =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    public static final UUID POLYMARKET_GAMMA =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000002"
            );

    public static final UUID POLYMARKET_CLOB =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000003"
            );

    private DataSourceIds() {
    }
}