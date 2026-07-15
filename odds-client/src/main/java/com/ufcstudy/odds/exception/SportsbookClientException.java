package com.ufcstudy.odds.exception;

public class SportsbookClientException extends RuntimeException {

    public SportsbookClientException(String message) {
        super(message);
    }

    public SportsbookClientException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}