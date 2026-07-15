package com.ufcstudy.odds.provider;

import com.ufcstudy.odds.model.SportsbookOddsBatch;
import com.ufcstudy.odds.model.SportsbookOddsRequest;

public interface SportsbookOddsClient {

    String providerCode();

    SportsbookOddsBatch fetchCurrentOdds(
            SportsbookOddsRequest request
    );
}