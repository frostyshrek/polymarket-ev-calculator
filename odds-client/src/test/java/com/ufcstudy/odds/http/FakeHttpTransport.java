package com.ufcstudy.odds.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class FakeHttpTransport implements HttpTransport {

    private final List<HttpTransportResponse> responses =
            new ArrayList<>();

    private final List<HttpTransportRequest> requests =
            new ArrayList<>();

    public void enqueue(HttpTransportResponse response) {
        responses.add(response);
    }

    public List<HttpTransportRequest> requests() {
        return List.copyOf(requests);
    }

    @Override
    public HttpTransportResponse get(HttpTransportRequest request)
            throws IOException {

        requests.add(request);

        if (responses.isEmpty()) {
            throw new IOException(
                    "No fake response was configured"
            );
        }

        return responses.removeFirst();
    }
}