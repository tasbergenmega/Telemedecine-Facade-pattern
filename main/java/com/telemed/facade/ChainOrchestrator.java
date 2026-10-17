package com.telemed.facade;

import com.telemed.db.Store;
import com.telemed.domain.AppointmentStatus;
import com.telemed.domain.ChainStatus;
import com.telemed.domain.PrescriptionStatus;
import com.telemed.domain.SessionStatus;
import com.telemed.exception.CompensationException;
import com.telemed.subsystemA.Appointment;
import com.telemed.subsystemA.SessionNote;
import com.telemed.subsystemA.SubsystemARepository;
import com.telemed.subsystemB.Prescription;
import com.telemed.subsystemB.SubsystemBRepository;
import com.telemed.subsystemB.VideoSession;
import com.telemed.subsystemC.SubsystemCRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public class ChainOrchestrator {

    private final Store store;
    private final SubsystemARepository repoA;
    private final SubsystemBRepository repoB;
    private final SubsystemCRepository repoC;

    public ChainOrchestrator(Store store,
                             SubsystemARepository repoA,
                             SubsystemBRepository repoB,
                             SubsystemCRepository repoC) {
        this.store = store;
        this.repoA = repoA;
        this.repoB = repoB;
        this.repoC = repoC;
    }

    public void createChain(String chainId) {
        store.execute("""
            INSERT INTO chain_state (chain_id, status, updated_at)
            VALUES (?, ?, ?)
        """, chainId, ChainStatus.PENDING_A.name(), LocalDateTime.now().toString());
    }

    public void updateChain(String chainId, ChainStatus status,
                            String appointmentId, String sessionId,
                            String prescriptionId, String billingId,
                            String lastError) {
        store.execute("""
            UPDATE chain_state
               SET status = ?, appointment_id = ?, session_id = ?,
                   prescription_id = ?, billing_id = ?, last_error = ?, updated_at = ?
             WHERE chain_id = ?
        """, status.name(), appointmentId, sessionId,
                prescriptionId, billingId, lastError,
                LocalDateTime.now().toString(), chainId);
    }

    public ChainStatus readStatus(String chainId) {
        Object[] row = store.queryOne(
                "SELECT status FROM chain_state WHERE chain_id = ?", chainId);
        if (row == null) return null;
        return ChainStatus.valueOf((String) row[0]);
    }

    public Object[] readChainRow(String chainId) {
        return store.queryOne(
                "SELECT status, appointment_id, session_id, prescription_id, billing_id " +
                        "FROM chain_state WHERE chain_id = ?", chainId);
    }

    public void compensateA(String appointmentId) {
        try {
            Appointment a = repoA.findById(appointmentId);
            if (a != null && a.getStatus() != AppointmentStatus.CANCELLED) {
                repoA.updateStatus(appointmentId, AppointmentStatus.CANCELLED);
            }
            SessionNote n = repoA.findNoteByAppointment(appointmentId);
            if (n != null && n.getStatus() != SessionStatus.CANCELLED) {
                repoA.updateNoteStatus(n.getId(), SessionStatus.CANCELLED);
            }
        } catch (RuntimeException ex) {
            throw new CompensationException(
                    "Failed to compensate A (appointment=" + appointmentId + ")", ex);
        }
    }

    public void compensateB(String appointmentId) {
        try {
            VideoSession s = repoB.findSessionByAppointment(appointmentId);
            if (s != null) {
                Prescription p = repoB.findPrescriptionBySession(s.getId());
                if (p != null && p.getStatus() != PrescriptionStatus.VOIDED) {
                    repoB.updatePrescriptionStatus(p.getId(), PrescriptionStatus.VOIDED);
                }
                if (s.getStatus() != SessionStatus.FAILED) {
                    repoB.updateSessionStatus(s.getId(), SessionStatus.FAILED,
                            s.getStartedAt(), LocalDateTime.now());
                }
            }
        } catch (RuntimeException ex) {
            throw new CompensationException(
                    "Failed to compensate B (appointment=" + appointmentId + ")", ex);
        }
    }

    public static String newChainId() {
        return UUID.randomUUID().toString();
    }
}