package com.telemed.domain;

public enum PrescriptionStatus {
    DRAFT, ISSUED, VOIDED;

    public boolean canTransitionTo(PrescriptionStatus next) {
        return switch (this) {
            case DRAFT  -> next == ISSUED || next == VOIDED;
            case ISSUED -> next == VOIDED;
            case VOIDED -> false;
        };
    }
}