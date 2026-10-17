package com.telemed;

import com.telemed.db.Store;
import com.telemed.exception.BillingRejectedException;
import com.telemed.exception.InvalidIntakeException;
import com.telemed.exception.VideoCallFailedException;
import com.telemed.facade.AdminFacade;
import com.telemed.facade.ChainOrchestrator;
import com.telemed.facade.ConsultationFacade;
import com.telemed.subsystemA.SubsystemARepository;
import com.telemed.subsystemB.SubsystemBRepository;
import com.telemed.subsystemC.SubsystemCRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Store store = new Store("telemed.db");

        SubsystemARepository repoA = new SubsystemARepository(store);
        SubsystemBRepository repoB = new SubsystemBRepository(store);
        SubsystemCRepository repoC = new SubsystemCRepository(store);
        ChainOrchestrator orchestrator = new ChainOrchestrator(store, repoA, repoB, repoC);

        ConsultationFacade consultation = new ConsultationFacade(repoA, repoB, repoC, orchestrator);
        AdminFacade admin = new AdminFacade(store, repoA, repoB, repoC, orchestrator);

        Scanner sc = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> runHappyPath(consultation, sc);
                    case "2" -> showChains(admin);
                    case "3" -> showAll(admin);
                    case "4" -> runBFailure(consultation);
                    case "5" -> runCFailure(consultation);
                    case "6" -> showPending(admin);
                    case "7" -> {
                        System.out.print("chain_id for manual compensation: ");
                        String id = sc.nextLine().trim();
                        admin.forceCompensate(id);
                        System.out.println("OK: compensation completed");
                    }
                    case "8" -> {
                        admin.resetStore();
                        System.out.println("OK: store cleared");
                    }
                    case "0" -> {
                        store.close();
                        System.out.println("Bye.");
                        return;
                    }
                    default -> System.out.println("Unknown command.");
                }
            } catch (InvalidIntakeException | VideoCallFailedException
                     | BillingRejectedException ex) {
                System.out.println("EXPECTED ERROR: " + ex.getMessage());
            } catch (RuntimeException ex) {
                System.out.println("UNEXPECTED ERROR: " + ex);
            }
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("=== Telemedicine Console ===");
        System.out.println("1) Run consultation (A->B->C) - happy path");
        System.out.println("2) Show all chain_state");
        System.out.println("3) Show all records (appointments/sessions/prescriptions/billing)");
        System.out.println("4) Force failure B (video call failed)");
        System.out.println("5) Force failure C (insurance rejected)");
        System.out.println("6) Show pending chains (PENDING_*)");
        System.out.println("7) Manual compensation by chain_id");
        System.out.println("8) Reset store");
        System.out.println("0) Exit");
        System.out.print("> ");
    }

    private static ConsultationFacade.ConsultationRequest buildRequest(
            String networkId, boolean validPayment, boolean insuranceApproved) {
        return new ConsultationFacade.ConsultationRequest(
                "DR-001",
                "PAT-777",
                "KZ",
                LocalDateTime.now().plusHours(1),
                true,
                networkId,
                "ASP-100",
                100,
                "none",
                10_000,
                80,
                validPayment,
                insuranceApproved
        );
    }

    private static void runHappyPath(ConsultationFacade facade, Scanner sc) {
        System.out.print("networkId [offline/weak/medium/good or 0/1/2/3]: ");
        String net = sc.nextLine().trim();
        ConsultationFacade.ConsultationResult r =
                facade.runConsultation(buildRequest(net, true, true));
        System.out.println("OK: chain=" + r.chainId() + " status=" + r.chainStatus());
    }

    private static void runBFailure(ConsultationFacade facade) {
        System.out.println("Force B failure: networkId=offline");
        facade.runConsultation(buildRequest("offline", true, true));
    }

    private static void runCFailure(ConsultationFacade facade) {
        System.out.println("Force C failure: insuranceApproved=false");
        facade.runConsultation(buildRequest("good", true, false));
    }

    private static void showChains(AdminFacade admin) {
        System.out.println("chain_id | status | appt | session | presc | bill | error | updated");
        for (Object[] row : admin.listChains()) {
            System.out.println(String.join(" | ",
                    row[0] + "", row[1] + "", row[2] + "", row[3] + "",
                    row[4] + "", row[5] + "", row[6] + "", row[7] + ""));
        }
    }

    private static void showPending(AdminFacade admin) {
        List<Object[]> rows = admin.pendingChains();
        if (rows.isEmpty()) { System.out.println("(no pending items)"); return; }
        for (Object[] row : rows) {
            System.out.println(String.join(" | ", row[0] + "", row[1] + "", row[2] + ""));
        }
    }

    private static void showAll(AdminFacade admin) {
        showChains(admin);
    }
}