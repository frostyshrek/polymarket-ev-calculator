package com.ufcstudy.odds.exception;

public final class SportsbookResponseException
        extends SportsbookClientException {

    private final int statusCode;

    public SportsbookResponseException(
            int statusCode,
            String message
    ) {
        super(message);
        this.statusCode = statusCode;
    }

    public int statusCode() {
        return statusCode;
    }
}