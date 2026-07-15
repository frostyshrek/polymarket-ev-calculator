package com.ufcstudy.polymarket.orderbook.provider;

import com.ufcstudy.polymarket.orderbook.model.PolymarketOrderBookSnapshot;

public interface PolymarketOrderBookClient {

    PolymarketOrderBookSnapshot fetchOrderBook(
            String tokenId
    );
}