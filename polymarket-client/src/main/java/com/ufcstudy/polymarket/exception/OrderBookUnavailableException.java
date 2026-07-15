package com.ufcstudy.polymarket.exception;

public final class OrderBookUnavailableException
        extends PolymarketClientException {

    private final String tokenId;

    public OrderBookUnavailableException(
            String tokenId,
            String message
    ) {
        super(message);
        this.tokenId = tokenId;
    }

    public String tokenId() {
        return tokenId;
    }
}