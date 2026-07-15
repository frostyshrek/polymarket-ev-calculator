package com.ufcstudy.odds.model;

public enum OddsFormat {
    DECIMAL("decimal"),
    AMERICAN("american");

    private final String apiValue;

    OddsFormat(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }
}