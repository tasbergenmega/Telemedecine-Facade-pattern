package com.telemed.subsystemB;

import com.telemed.db.Store;
import com.telemed.domain.PrescriptionStatus;
import com.telemed.domain.SessionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubsystemBRepository {

    private final Store store;

    public SubsystemBRepository(Store store) { this.store = store; }

    public void save(VideoSession s) {
        store.execute("""
            INSERT INTO video_sessions (id, appointment_id, status, started_at, ended_at)
            VALUES (?, ?, ?, ?, ?)
        """, s.getId(), s.getAppointmentId(), s.getStatus().name(),
                s.getStartedAt() == null ? null : s.getStartedAt().toString(),
                s.getEndedAt() == null ? null : s.getEndedAt().toString());
    }

    public void updateSessionStatus(String sessionId, SessionStatus status,
                                    LocalDateTime startedAt, LocalDateTime endedAt) {
        store.execute("""
            UPDATE video_sessions SET status = ?, started_at = ?, ended_at = ?
            WHERE id = ?
        """, status.name(),
                startedAt == null ? null : startedAt.toString(),
                endedAt == null ? null : endedAt.toString(),
                sessionId);
    }

    public VideoSession findSessionById(String sessionId) {
        Object[] row = store.queryOne(
                "SELECT id, appointment_id, status, started_at, ended_at " +
                        "FROM video_sessions WHERE id = ?", sessionId);
        if (row == null) return null;
        return new VideoSession((String) row[0], (String) row[1],
                SessionStatus.valueOf((String) row[2]),
                row[3] == null ? null : LocalDateTime.parse((String) row[3]),
                row[4] == null ? null : LocalDateTime.parse((String) row[4]));
    }

    public VideoSession findSessionByAppointment(String appointmentId) {
        Object[] row = store.queryOne(
                "SELECT id, appointment_id, status, started_at, ended_at " +
                        "FROM video_sessions WHERE appointment_id = ?", appointmentId);
        if (row == null) return null;
        return new VideoSession((String) row[0], (String) row[1],
                SessionStatus.valueOf((String) row[2]),
                row[3] == null ? null : LocalDateTime.parse((String) row[3]),
                row[4] == null ? null : LocalDateTime.parse((String) row[4]));
    }

    public void save(Prescription p) {
        store.execute("""
            INSERT INTO prescriptions (id, session_id, drug_code, dosage_mg, status)
            VALUES (?, ?, ?, ?, ?)
        """, p.getId(), p.getSessionId(), p.getDrugCode(), p.getDosageMg(),
                p.getStatus().name());
    }

    public void updatePrescriptionStatus(String prescriptionId, PrescriptionStatus status) {
        store.execute("UPDATE prescriptions SET status = ? WHERE id = ?",
                status.name(), prescriptionId);
    }

    public Prescription findPrescriptionById(String id) {
        Object[] row = store.queryOne(
                "SELECT id, session_id, drug_code, dosage_mg, status " +
                        "FROM prescriptions WHERE id = ?", id);
        if (row == null) return null;
        return new Prescription((String) row[0], (String) row[1],
                (String) row[2], (Integer) row[3],
                PrescriptionStatus.valueOf((String) row[4]));
    }

    public Prescription findPrescriptionBySession(String sessionId) {
        Object[] row = store.queryOne(
                "SELECT id, session_id, drug_code, dosage_mg, status " +
                        "FROM prescriptions WHERE session_id = ?", sessionId);
        if (row == null) return null;
        return new Prescription((String) row[0], (String) row[1],
                (String) row[2], (Integer) row[3],
                PrescriptionStatus.valueOf((String) row[4]));
    }

    public List<Prescription> findAllPrescriptions() {
        List<Prescription> out = new ArrayList<>();
        for (Object[] row : store.query(
                "SELECT id, session_id, drug_code, dosage_mg, status FROM prescriptions")) {
            out.add(new Prescription((String) row[0], (String) row[1],
                    (String) row[2], (Integer) row[3],
                    PrescriptionStatus.valueOf((String) row[4])));
        }
        return out;
    }
}