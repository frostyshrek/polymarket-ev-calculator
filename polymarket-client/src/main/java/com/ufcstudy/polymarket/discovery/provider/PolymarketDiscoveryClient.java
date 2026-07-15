package com.ufcstudy.polymarket.discovery.provider;

import com.ufcstudy.polymarket.discovery.model.PolymarketDiscoveryBatch;

public interface PolymarketDiscoveryClient {

    PolymarketDiscoveryBatch fetchActiveEvents(
            int limit,
            int offset,
            String tagId
    );
}