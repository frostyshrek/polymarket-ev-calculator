package com.ufcstudy.polymarket.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public final class JdkPolymarketHttpTransport
        implements PolymarketHttpTransport {

    private final HttpClient httpClient;

    public JdkPolymarketHttpTransport(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    @Override
    public PolymarketHttpResponse get(
            PolymarketHttpRequest request
    ) throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(request.uri())
                .timeout(request.timeout())
                .GET();

        request.headers().forEach(builder::header);

        HttpResponse<String> response = httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        return new PolymarketHttpResponse(
                response.statusCode(),
                response.body(),
                response.headers().map()
        );
    }
}