package com.ufcstudy.polymarket.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FakePolymarketHttpTransport
        implements PolymarketHttpTransport {

    private final List<PolymarketHttpResponse> responses =
            new ArrayList<>();

    private final List<PolymarketHttpRequest> requests =
            new ArrayList<>();

    public void enqueue(PolymarketHttpResponse response) {
        responses.add(response);
    }

    public List<PolymarketHttpRequest> requests() {
        return List.copyOf(requests);
    }

    @Override
    public PolymarketHttpResponse get(
            PolymarketHttpRequest request
    ) throws IOException {

        requests.add(request);

        if (responses.isEmpty()) {
            throw new IOException(
                    "No fake Polymarket response was configured"
            );
        }

        return responses.removeFirst();
    }
}