package com.telemed.subsystemA;

import com.telemed.domain.SessionStatus;

import java.util.UUID;

public class SessionNote {

    private final String id;
    private final String appointmentId;
    private String content;
    private SessionStatus status;

    public SessionNote(String appointmentId, String content) {
        this.id = UUID.randomUUID().toString();
        this.appointmentId = appointmentId;
        this.content = content;
        this.status = SessionStatus.DRAFT;
    }

    public SessionNote(String id, String appointmentId, String content, SessionStatus status) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.content = content;
        this.status = status;
    }

    public void transitionTo(SessionStatus next) {
        if (!status.canTransitionTo(next)) {
            throw new IllegalStateException(
                    "Invalid SessionNote transition: " + status + " -> " + next);
        }
        this.status = next;
    }

    public void updateContent(String newContent) {
        if (status != SessionStatus.DRAFT) {
            throw new IllegalStateException("Notes can only be edited in DRAFT status");
        }
        this.content = newContent;
    }

    public String getId() { return id; }
    public String getAppointmentId() { return appointmentId; }
    public String getContent() { return content; }
    public SessionStatus getStatus() { return status; }
}