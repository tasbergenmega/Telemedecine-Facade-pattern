package com.telemed.facade;

import com.telemed.domain.AppointmentStatus;
import com.telemed.domain.BillingStatus;
import com.telemed.domain.ChainStatus;
import com.telemed.domain.PrescriptionStatus;
import com.telemed.domain.SessionStatus;
import com.telemed.exception.BillingRejectedException;
import com.telemed.exception.VideoCallFailedException;
import com.telemed.subsystemA.Appointment;
import com.telemed.subsystemA.AppointmentValidator;
import com.telemed.subsystemA.SessionNote;
import com.telemed.subsystemA.SlotConflictChecker;
import com.telemed.subsystemA.SubsystemARepository;
import com.telemed.subsystemB.NetworkQualityDecider;
import com.telemed.subsystemB.Prescription;
import com.telemed.subsystemB.PrescriptionRulesEngine;
import com.telemed.subsystemB.SubsystemBRepository;
import com.telemed.subsystemB.VideoSession;
import com.telemed.subsystemC.BillingRecord;
import com.telemed.subsystemC.BillingValidator;
import com.telemed.subsystemC.InsuranceCoverageCalculator;
import com.telemed.subsystemC.SubsystemCRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Main facade for one consultation operation across three independent subsystems.
 */
public class ConsultationFacade {

    private final SubsystemARepository repoA;
    private final SubsystemBRepository repoB;
    private final SubsystemCRepository repoC;
    private final ChainOrchestrator orchestrator;

    private final AppointmentValidator appointmentValidator = new AppointmentValidator();
    private final SlotConflictChecker slotConflictChecker = new SlotConflictChecker();
    private final PrescriptionRulesEngine prescriptionRules = new PrescriptionRulesEngine();
    private final NetworkQualityDecider networkQuality = new NetworkQualityDecider();
    private final InsuranceCoverageCalculator coverage = new InsuranceCoverageCalculator();
    private final BillingValidator billingValidator = new BillingValidator();

    public ConsultationFacade(SubsystemARepository repoA,
                              SubsystemBRepository repoB,
                              SubsystemCRepository repoC,
                              ChainOrchestrator orchestrator) {
        this.repoA = repoA;
        this.repoB = repoB;
        this.repoC = repoC;
        this.orchestrator = orchestrator;
    }

    public record ConsultationResult(String chainId,
                                     String appointmentId,
                                     String sessionId,
                                     String prescriptionId,
                                     String billingId,
                                     ChainStatus chainStatus) { }

    public record ConsultationRequest(String doctorId,
                                      String patientId,
                                      String region,
                                      LocalDateTime slot,
                                      boolean doctorLicensedInRegion,
                                      String networkId,
                                      String drugCode,
                                      int dosageMg,
                                      String patientAllergies,
                                      int baseAmountCents,
                                      int coveragePercent,
                                      boolean hasValidPaymentMethod,
                                      boolean insuranceApproved) { }

    public ConsultationResult runConsultation(ConsultationRequest req) {

        appointmentValidator.validate(req.doctorId(), req.patientId(),
                req.region(), req.doctorLicensedInRegion());

        List<Appointment> existing = repoA.findByDoctor(req.doctorId());
        slotConflictChecker.ensureSlotIsFree(req.doctorId(), req.slot(), existing);

        Appointment appointment = new Appointment(
                req.doctorId(), req.patientId(), req.slot(), req.region());
        appointment.transitionTo(AppointmentStatus.CONFIRMED);
        repoA.save(appointment);

        SessionNote note = new SessionNote(appointment.getId(), "Auto-draft");
        repoA.save(note);

        String chainId = ChainOrchestrator.newChainId();
        orchestrator.createChain(chainId);
        orchestrator.updateChain(chainId, ChainStatus.PENDING_B,
                appointment.getId(), null, null, null, null);

        try {
            int quality = networkQuality.estimateQuality(req.networkId());
            networkQuality.ensureCallPossible(quality);

            prescriptionRules.ensurePrescriptionAllowed(req.drugCode(), req.dosageMg());
            prescriptionRules.ensureNoInteraction(req.drugCode(), req.patientAllergies());

            VideoSession session = new VideoSession(appointment.getId());
            session.transitionTo(SessionStatus.ACTIVE);
            repoB.save(session);

            Prescription prescription = new Prescription(
                    session.getId(), req.drugCode(), req.dosageMg());
            prescription.transitionTo(PrescriptionStatus.ISSUED);
            repoB.save(prescription);

            orchestrator.updateChain(chainId, ChainStatus.PENDING_C,
                    appointment.getId(), session.getId(), prescription.getId(),
                    null, null);

            try {
                int patientShare = coverage.patientShareCents(
                        req.baseAmountCents(), req.coveragePercent());
                billingValidator.ensureBillable(req.hasValidPaymentMethod(),
                        req.insuranceApproved(), patientShare);

                BillingRecord billing = new BillingRecord(
                        appointment.getId(), patientShare, "EUR");
                billing.transitionTo(BillingStatus.PAID);
                repoC.save(billing);

                orchestrator.updateChain(chainId, ChainStatus.COMPLETED,
                        appointment.getId(), session.getId(), prescription.getId(),
                        billing.getId(), null);

                return new ConsultationResult(chainId, appointment.getId(),
                        session.getId(), prescription.getId(), billing.getId(),
                        ChainStatus.COMPLETED);

            } catch (BillingRejectedException cEx) {
                orchestrator.compensateB(appointment.getId());
                orchestrator.compensateA(appointment.getId());
                orchestrator.updateChain(chainId, ChainStatus.COMPENSATED,
                        appointment.getId(), session.getId(), prescription.getId(),
                        null, cEx.getMessage());
                throw cEx;
            }

        } catch (VideoCallFailedException bEx) {
            orchestrator.compensateA(appointment.getId());
            orchestrator.updateChain(chainId, ChainStatus.COMPENSATED,
                    appointment.getId(), null, null, null, bEx.getMessage());
            throw bEx;
        }
    }
}