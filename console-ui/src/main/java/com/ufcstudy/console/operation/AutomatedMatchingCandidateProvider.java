package com.ufcstudy.console.operation;

import com.ufcstudy.eventmatching.automated.AutomatedMarketCandidate;

import java.util.List;

public interface AutomatedMatchingCandidateProvider {

    List<AutomatedMarketCandidate> findCandidates();
}