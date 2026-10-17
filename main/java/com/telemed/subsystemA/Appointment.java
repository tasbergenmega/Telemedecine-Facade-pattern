package com.telemed.subsystemA;

import com.telemed.domain.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointment {

    private final String id;
    private final String doctorId;
    private final String patientId;
    private final LocalDateTime slot;
    private final String region;
    private AppointmentStatus status;
    private final LocalDateTime createdAt;

    public Appointment(String doctorId, String patientId, LocalDateTime slot, String region) {
        this.id = UUID.randomUUID().toString();
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.slot = slot;
        this.region = region;
        this.status = AppointmentStatus.NEW;
        this.createdAt = LocalDateTime.now();
    }

    public Appointment(String id, String doctorId, String patientId,
                       LocalDateTime slot, String region,
                       AppointmentStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.slot = slot;
        this.region = region;
        this.status = status;
        this.createdAt = createdAt;
    }

    public void transitionTo(AppointmentStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Invalid Appointment transition: " + status + " -> " + next);
        }
        this.status = next;
    }

    public String getId() { return id; }
    public String getDoctorId() { return doctorId; }
    public String getPatientId() { return patientId; }
    public LocalDateTime getSlot() { return slot; }
    public String getRegion() { return region; }
    public AppointmentStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}