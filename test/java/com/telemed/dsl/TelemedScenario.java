package com.telemed.dsl;

import com.telemed.db.Store;
import com.telemed.facade.ChainOrchestrator;
import com.telemed.facade.ConsultationFacade;
import com.telemed.subsystemA.SubsystemARepository;
import com.telemed.subsystemB.SubsystemBRepository;
import com.telemed.subsystemC.SubsystemCRepository;

import java.time.LocalDateTime;

public class TelemedScenario {

    public Store store;
    public SubsystemARepository repoA;
    public SubsystemBRepository repoB;
    public SubsystemCRepository repoC;
    public ChainOrchestrator orchestrator;
    public ConsultationFacade facade;

    public String doctorId = "DR-001";
    public String patientId = "PAT-777";
    public String region = "KZ";
    public LocalDateTime slot = LocalDateTime.now().plusHours(1);
    public boolean licensed = true;
    public String network = "good";
    public String drug = "ASP-100";
    public int dosage = 100;
    public String allergies = "none";
    public int amount = 10000;
    public int coverage = 80;
    public boolean validPayment = true;
    public boolean insuranceApproved = true;

    public TelemedScenario(String dbPath) {
        this.store = new Store(dbPath);
        this.repoA = new SubsystemARepository(store);
        this.repoB = new SubsystemBRepository(store);
        this.repoC = new SubsystemCRepository(store);
        this.orchestrator = new ChainOrchestrator(store, repoA, repoB, repoC);
        this.facade = new ConsultationFacade(repoA, repoB, repoC, orchestrator);
    }

    public TelemedScenario givenOfflineNetwork() { this.network = "offline"; return this; }
    public TelemedScenario givenRejectedInsurance() { this.insuranceApproved = false; return this; }
    public TelemedScenario givenUnlicensedDoctor() { this.licensed = false; return this; }

    public ConsultationFacade.ConsultationResult whenBookingConsultation() {
        ConsultationFacade.ConsultationRequest req = new ConsultationFacade.ConsultationRequest(
                doctorId, patientId, region, slot, licensed, network,
                drug, dosage, allergies, amount, coverage, validPayment, insuranceApproved
        );
        return facade.runConsultation(req);
    }

    public String lastChainId() {
        Object[] row = store.queryOne("SELECT chain_id FROM chain_state ORDER BY updated_at DESC LIMIT 1");
        return row == null ? null : (String) row[0];
    }

    public String chainStatus() {
        Object[] row = store.queryOne("SELECT status FROM chain_state ORDER BY updated_at DESC LIMIT 1");
        return row == null ? null : (String) row[0];
    }

    public String appointmentStatus() {
        Object[] row = store.queryOne("SELECT status FROM appointments ORDER BY created_at DESC LIMIT 1");
        return row == null ? null : (String) row[0];
    }

    public String sessionStatus() {
        Object[] row = store.queryOne("SELECT status FROM video_sessions ORDER BY started_at DESC LIMIT 1");
        return row == null ? null : (String) row[0];
    }

    public String prescriptionStatus() {
        Object[] row = store.queryOne("SELECT status FROM prescriptions LIMIT 1");
        return row == null ? null : (String) row[0];
    }

    public void close() {
        store.close();
    }
}