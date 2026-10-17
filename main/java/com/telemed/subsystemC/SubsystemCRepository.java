package com.telemed.subsystemC;

import com.telemed.db.Store;
import com.telemed.domain.BillingStatus;

import java.util.ArrayList;
import java.util.List;

public class SubsystemCRepository {

    private final Store store;

    public SubsystemCRepository(Store store) { this.store = store; }

    public void save(BillingRecord b) {
        store.execute("""
            INSERT INTO billing_records (id, appointment_id, amount_cents, currency, status)
            VALUES (?, ?, ?, ?, ?)
        """, b.getId(), b.getAppointmentId(), b.getAmountCents(),
                b.getCurrency(), b.getStatus().name());
    }

    public void updateStatus(String billingId, BillingStatus status) {
        store.execute("UPDATE billing_records SET status = ? WHERE id = ?",
                status.name(), billingId);
    }

    public BillingRecord findById(String billingId) {
        Object[] row = store.queryOne(
                "SELECT id, appointment_id, amount_cents, currency, status " +
                        "FROM billing_records WHERE id = ?", billingId);
        if (row == null) return null;
        return new BillingRecord(
                (String) row[0], (String) row[1],
                (Integer) row[2], (String) row[3],
                BillingStatus.valueOf((String) row[4]));
    }

    public BillingRecord findByAppointment(String appointmentId) {
        Object[] row = store.queryOne(
                "SELECT id, appointment_id, amount_cents, currency, status " +
                        "FROM billing_records WHERE appointment_id = ?", appointmentId);
        if (row == null) return null;
        return new BillingRecord(
                (String) row[0], (String) row[1],
                (Integer) row[2], (String) row[3],
                BillingStatus.valueOf((String) row[4]));
    }

    public List<BillingRecord> findAll() {
        List<BillingRecord> out = new ArrayList<>();
        for (Object[] row : store.query(
                "SELECT id, appointment_id, amount_cents, currency, status FROM billing_records")) {
            out.add(new BillingRecord(
                    (String) row[0], (String) row[1],
                    (Integer) row[2], (String) row[3],
                    BillingStatus.valueOf((String) row[4])));
        }
        return out;
    }
}