package com.ufcstudy.polymarket.http;

import java.io.IOException;

public interface PolymarketHttpTransport {

    PolymarketHttpResponse get(PolymarketHttpRequest request)
            throws IOException, InterruptedException;
}