package com.telemed.subsystemB;

import com.telemed.domain.SessionStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class VideoSession {

    private final String id;
    private final String appointmentId;
    private SessionStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;

    public VideoSession(String appointmentId) {
        this.id = UUID.randomUUID().toString();
        this.appointmentId = appointmentId;
        this.status = SessionStatus.DRAFT;
    }

    public VideoSession(String id, String appointmentId, SessionStatus status,
                        LocalDateTime startedAt, LocalDateTime endedAt) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.status = status;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public void transitionTo(SessionStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Invalid VideoSession transition: " + status + " -> " + next);
        }
        this.status = next;
        if (next == SessionStatus.ACTIVE && startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
        if ((next == SessionStatus.ENDED || next == SessionStatus.FAILED
                || next == SessionStatus.CANCELLED) && endedAt == null) {
            this.endedAt = LocalDateTime.now();
        }
    }

    public String getId() { return id; }
    public String getAppointmentId() { return appointmentId; }
    public SessionStatus getStatus() { return status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
}