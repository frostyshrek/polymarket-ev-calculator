package com.ufcstudy.odds.http;

import java.io.IOException;

public interface HttpTransport {

    HttpTransportResponse get(HttpTransportRequest request)
            throws IOException, InterruptedException;
}