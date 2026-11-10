package com.example.recruitment_service.Application;


import java.util.EnumSet;
import java.util.Set;

public enum ApplicationStatus {
    SUBMITTED,
    IN_REVIEW,
    INTERVIEW,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    public Set<ApplicationStatus> allowedNext() {
        return switch (this) {
            case SUBMITTED -> EnumSet.of(IN_REVIEW, REJECTED, WITHDRAWN);
            case IN_REVIEW -> EnumSet.of(INTERVIEW, REJECTED, WITHDRAWN);
            case INTERVIEW -> EnumSet.of(ACCEPTED, REJECTED, WITHDRAWN);
            case ACCEPTED, REJECTED, WITHDRAWN -> EnumSet.noneOf(ApplicationStatus.class);
        };
    }

    public boolean canTransitionTo(ApplicationStatus next) {
        return allowedNext().contains(next);
    }
}