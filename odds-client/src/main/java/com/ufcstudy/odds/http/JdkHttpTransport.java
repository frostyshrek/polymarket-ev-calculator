package com.ufcstudy.odds.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public final class JdkHttpTransport implements HttpTransport {

    private final HttpClient httpClient;

    public JdkHttpTransport(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient);
    }

    @Override
    public HttpTransportResponse get(HttpTransportRequest request)
            throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(request.uri())
                .timeout(request.timeout())
                .GET();

        request.headers().forEach(builder::header);

        HttpResponse<String> response = httpClient.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        return new HttpTransportResponse(
                response.statusCode(),
                response.body(),
                response.headers().map()
        );
    }
}