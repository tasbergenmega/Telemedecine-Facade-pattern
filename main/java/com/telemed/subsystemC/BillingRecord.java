package com.telemed.subsystemC;

import com.telemed.domain.BillingStatus;

import java.util.UUID;

public class BillingRecord {

    private final String id;
    private final String appointmentId;
    private final int amountCents;
    private final String currency;
    private BillingStatus status;

    public BillingRecord(String appointmentId, int amountCents, String currency) {
        this.id = UUID.randomUUID().toString();
        this.appointmentId = appointmentId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.status = BillingStatus.PENDING;
    }

    public BillingRecord(String id, String appointmentId, int amountCents,
                         String currency, BillingStatus status) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.amountCents = amountCents;
        this.currency = currency;
        this.status = status;
    }

    public void transitionTo(BillingStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Invalid BillingRecord transition: " + status + " -> " + next);
        }
        this.status = next;
    }

    public String getId() { return id; }
    public String getAppointmentId() { return appointmentId; }
    public int getAmountCents() { return amountCents; }
    public String getCurrency() { return currency; }
    public BillingStatus getStatus() { return status; }
}