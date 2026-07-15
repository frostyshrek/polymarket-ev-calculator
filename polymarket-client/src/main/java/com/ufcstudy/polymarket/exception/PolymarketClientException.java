package com.ufcstudy.polymarket.exception;

public class PolymarketClientException extends RuntimeException {

    public PolymarketClientException(String message) {
        super(message);
    }

    public PolymarketClientException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}