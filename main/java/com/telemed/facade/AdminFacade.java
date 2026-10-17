package com.telemed.facade;

import com.telemed.db.Store;
import com.telemed.domain.ChainStatus;
import com.telemed.subsystemA.SubsystemARepository;
import com.telemed.subsystemB.SubsystemBRepository;
import com.telemed.subsystemC.SubsystemCRepository;

import java.util.List;

public class AdminFacade {

    private final Store store;
    private final SubsystemARepository repoA;
    private final SubsystemBRepository repoB;
    private final SubsystemCRepository repoC;
    private final ChainOrchestrator orchestrator;

    public AdminFacade(Store store,
                       SubsystemARepository repoA,
                       SubsystemBRepository repoB,
                       SubsystemCRepository repoC,
                       ChainOrchestrator orchestrator) {
        this.store = store;
        this.repoA = repoA;
        this.repoB = repoB;
        this.repoC = repoC;
        this.orchestrator = orchestrator;
    }

    public List<Object[]> listChains() {
        return store.query(
                "SELECT chain_id, status, appointment_id, session_id, " +
                        "prescription_id, billing_id, last_error, updated_at " +
                        "FROM chain_state ORDER BY updated_at DESC");
    }

    public ChainStatus statusOf(String chainId) {
        return orchestrator.readStatus(chainId);
    }

    public List<Object[]> pendingChains() {
        return store.query(
                "SELECT chain_id, status, appointment_id, session_id, " +
                        "prescription_id, billing_id, last_error, updated_at " +
                        "FROM chain_state WHERE status IN ('PENDING_A','PENDING_B','PENDING_C') " +
                        "ORDER BY updated_at DESC");
    }

    public void forceCompensate(String chainId) {
        if (chainId == null || chainId.isBlank()) {
            throw new IllegalArgumentException("chain_id is not specified");
        }

        Object[] row = orchestrator.readChainRow(chainId);
        if (row == null) {
            throw new IllegalArgumentException("No such chain: " + chainId);
        }
        ChainStatus status = ChainStatus.valueOf((String) row[0]);
        String appointmentId = (String) row[1];

        switch (status) {
            case PENDING_C -> {
                if (appointmentId == null || appointmentId.isBlank()) {
                    throw new IllegalStateException("Chain " + chainId + " does not have an appointment_id");
                }
                orchestrator.compensateB(appointmentId);
                orchestrator.compensateA(appointmentId);
                orchestrator.updateChain(chainId, ChainStatus.COMPENSATED,
                        appointmentId, (String) row[2], (String) row[3],
                        (String) row[4], "manual compensation");
            }
            case PENDING_B -> {
                if (appointmentId == null || appointmentId.isBlank()) {
                    throw new IllegalStateException("Chain " + chainId + " does not have an appointment_id");
                }
                orchestrator.compensateA(appointmentId);
                orchestrator.updateChain(chainId, ChainStatus.COMPENSATED,
                        appointmentId, null, null, null, "manual compensation");
            }
            case PENDING_A, COMPLETED, COMPENSATED ->
                    throw new IllegalStateException(
                            "Chain is in status " + status + "; compensation is not needed or is not possible");
        }
    }

    public void resetStore() {
        store.execute("DELETE FROM billing_records");
        store.execute("DELETE FROM prescriptions");
        store.execute("DELETE FROM video_sessions");
        store.execute("DELETE FROM session_notes");
        store.execute("DELETE FROM appointments");
        store.execute("DELETE FROM chain_state");
    }
}