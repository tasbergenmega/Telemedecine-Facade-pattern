package com.telemed.domain;

public enum BillingStatus {
    PENDING, PAID, REJECTED, REFUNDED;

    public boolean canTransitionTo(BillingStatus next) {
        return switch (this) {
            case PENDING  -> next == PAID || next == REJECTED;
            case PAID     -> next == REFUNDED;
            case REJECTED, REFUNDED -> false;
        };
    }
}