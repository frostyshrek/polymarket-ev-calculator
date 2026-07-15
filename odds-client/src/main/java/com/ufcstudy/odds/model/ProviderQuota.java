package com.ufcstudy.odds.model;

public record ProviderQuota(
        Long requestsRemaining,
        Long requestsUsed,
        Long lastRequestCost
) {

    public boolean isKnown() {
        return requestsRemaining != null
                || requestsUsed != null
                || lastRequestCost != null;
    }

    public boolean isAtOrBelow(long threshold) {
        return requestsRemaining != null
                && requestsRemaining <= threshold;
    }
}