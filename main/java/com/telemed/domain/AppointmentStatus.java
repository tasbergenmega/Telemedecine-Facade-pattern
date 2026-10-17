package com.telemed.domain;

public enum AppointmentStatus {
    NEW, CONFIRMED, CANCELLED, COMPLETED;

    public boolean canTransitionTo(AppointmentStatus next) {
        return switch (this) {
            case NEW       -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == COMPLETED || next == CANCELLED;
            case CANCELLED, COMPLETED -> false;
        };
    }
}