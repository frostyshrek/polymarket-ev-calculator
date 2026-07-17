package com.ufcstudy.eventmatching.automated;

public record AutomatedMatchDecision(
        AutomatedMatchStatus status,
        String reason
) {
}