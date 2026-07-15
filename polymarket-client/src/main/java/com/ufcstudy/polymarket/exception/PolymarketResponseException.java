package com.ufcstudy.polymarket.exception;

public final class PolymarketResponseException
        extends PolymarketClientException {

    private final int statusCode;

    public PolymarketResponseException(
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