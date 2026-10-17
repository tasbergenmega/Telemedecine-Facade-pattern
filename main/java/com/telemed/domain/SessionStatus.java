package com.telemed.domain;

public enum SessionStatus {
    DRAFT, ACTIVE, ENDED, FAILED, CANCELLED;

    public boolean canTransitionTo(SessionStatus next) {
        return switch (this) {
            case DRAFT  -> next == ACTIVE || next == CANCELLED;
            case ACTIVE -> next == ENDED || next == FAILED || next == CANCELLED;
            case ENDED, FAILED, CANCELLED -> false;
        };
    }
}