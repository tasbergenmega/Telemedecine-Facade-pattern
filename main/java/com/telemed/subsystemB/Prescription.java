package com.telemed.subsystemB;

import com.telemed.domain.PrescriptionStatus;

import java.util.UUID;

public class Prescription {

    private final String id;
    private final String sessionId;
    private final String drugCode;
    private final int dosageMg;
    private PrescriptionStatus status;

    public Prescription(String sessionId, String drugCode, int dosageMg) {
        this.id = UUID.randomUUID().toString();
        this.sessionId = sessionId;
        this.drugCode = drugCode;
        this.dosageMg = dosageMg;
        this.status = PrescriptionStatus.DRAFT;
    }

    public Prescription(String id, String sessionId, String drugCode, int dosageMg,
                        PrescriptionStatus status) {
        this.id = id;
        this.sessionId = sessionId;
        this.drugCode = drugCode;
        this.dosageMg = dosageMg;
        this.status = status;
    }

    public void transitionTo(PrescriptionStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Invalid Prescription transition: " + status + " -> " + next);
        }
        this.status = next;
    }

    public String getId() { return id; }
    public String getSessionId() { return sessionId; }
    public String getDrugCode() { return drugCode; }
    public int getDosageMg() { return dosageMg; }
    public PrescriptionStatus getStatus() { return status; }
}