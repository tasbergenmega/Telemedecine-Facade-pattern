package com.telemed.subsystemA;

import com.telemed.db.Store;
import com.telemed.domain.AppointmentStatus;
import com.telemed.domain.SessionStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SubsystemARepository {

    private final Store store;

    public SubsystemARepository(Store store) { this.store = store; }

    public void save(Appointment a) {
        store.execute("""
            INSERT INTO appointments (id, doctor_id, patient_id, slot_iso, region, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """, a.getId(), a.getDoctorId(), a.getPatientId(),
                a.getSlot().toString(), a.getRegion(), a.getStatus().name(),
                a.getCreatedAt().toString());
    }

    public void updateStatus(String appointmentId, AppointmentStatus status) {
        store.execute("UPDATE appointments SET status = ? WHERE id = ?",
                status.name(), appointmentId);
    }

    public Appointment findById(String appointmentId) {
        Object[] row = store.queryOne(
                "SELECT id, doctor_id, patient_id, slot_iso, region, status, created_at " +
                        "FROM appointments WHERE id = ?", appointmentId);
        if (row == null) return null;
        return new Appointment(
                (String) row[0], (String) row[1], (String) row[2],
                LocalDateTime.parse((String) row[3]), (String) row[4],
                AppointmentStatus.valueOf((String) row[5]),
                LocalDateTime.parse((String) row[6]));
    }

    public List<Appointment> findByDoctor(String doctorId) {
        List<Appointment> out = new ArrayList<>();
        for (Object[] row : store.query(
                "SELECT id, doctor_id, patient_id, slot_iso, region, status, created_at " +
                        "FROM appointments WHERE doctor_id = ?", doctorId)) {
            out.add(new Appointment(
                    (String) row[0], (String) row[1], (String) row[2],
                    LocalDateTime.parse((String) row[3]), (String) row[4],
                    AppointmentStatus.valueOf((String) row[5]),
                    LocalDateTime.parse((String) row[6])));
        }
        return out;
    }

    public void save(SessionNote n) {
        store.execute("""
            INSERT INTO session_notes (id, appointment_id, content, status)
            VALUES (?, ?, ?, ?)
        """, n.getId(), n.getAppointmentId(), n.getContent(), n.getStatus().name());
    }

    public void updateNoteStatus(String noteId, SessionStatus status) {
        store.execute("UPDATE session_notes SET status = ? WHERE id = ?",
                status.name(), noteId);
    }

    public SessionNote findNoteById(String noteId) {
        Object[] row = store.queryOne(
                "SELECT id, appointment_id, content, status FROM session_notes WHERE id = ?",
                noteId);
        if (row == null) return null;
        return new SessionNote((String) row[0], (String) row[1],
                (String) row[2], SessionStatus.valueOf((String) row[3]));
    }

    public SessionNote findNoteByAppointment(String appointmentId) {
        Object[] row = store.queryOne(
                "SELECT id, appointment_id, content, status FROM session_notes WHERE appointment_id = ?",
                appointmentId);
        if (row == null) return null;
        return new SessionNote((String) row[0], (String) row[1],
                (String) row[2], SessionStatus.valueOf((String) row[3]));
    }
}