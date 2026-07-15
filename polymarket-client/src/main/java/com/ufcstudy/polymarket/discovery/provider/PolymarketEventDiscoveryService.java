package com.ufcstudy.polymarket.discovery.provider;

import com.ufcstudy.polymarket.discovery.model.PolymarketDiscoveryBatch;
import com.ufcstudy.polymarket.discovery.model.PolymarketEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PolymarketEventDiscoveryService {

    private final PolymarketDiscoveryClient client;
    private final int pageSize;
    private final int maximumPages;

    public PolymarketEventDiscoveryService(
            PolymarketDiscoveryClient client,
            int pageSize,
            int maximumPages
    ) {
        this.client = Objects.requireNonNull(client);

        if (pageSize < 1) {
            throw new IllegalArgumentException(
                    "Page size must be positive"
            );
        }

        if (maximumPages < 1) {
            throw new IllegalArgumentException(
                    "Maximum pages must be positive"
            );
        }

        this.pageSize = pageSize;
        this.maximumPages = maximumPages;
    }

    public List<PolymarketEvent> discoverActiveEvents(
            String tagId
    ) {
        List<PolymarketEvent> events = new ArrayList<>();
        int offset = 0;

        for (int page = 0; page < maximumPages; page++) {
            PolymarketDiscoveryBatch batch =
                    client.fetchActiveEvents(
                            pageSize,
                            offset,
                            tagId
                    );

            events.addAll(batch.events());

            if (!batch.mayHaveNextPage()) {
                break;
            }

            offset += pageSize;
        }

        return List.copyOf(events);
    }
}