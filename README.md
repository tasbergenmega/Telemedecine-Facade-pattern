# Telemedicine Facade (Assignment 4)

## Overview
This project implements a Facade layer over three independent subsystems (A, B, C) for the Telemedicine domain. It orchestrates a chain: Appointment Schedule (A) → Video Call Manager / E-Prescription (B) → Billing Record (C). The system features durable state (SQLite) and cross-subsystem compensation.

## How to Run & Test
**Prerequisites:** Java 17 or 21, Maven.
- **Run the CLI application:** `mvn exec:java` (or run `Main.java` directly in IDE).
- **Run tests:** `mvn clean test`
- **Generate coverage report:** `mvn clean test jacoco:report` (view at `target/site/jacoco/index.html`).
- **Reset DB:** The application creates `telemed.db` automatically. To reset, delete `telemed.db` and restart, or use option 8 in the CLI menu.

## Principle of Least Knowledge: Before / After

### Before (Without Facade)
A client would have to know and coordinate all three subsystems, their repositories, and manually handle rollbacks:
```java
// Deep chain, many friends
AppointmentValidator validator = new AppointmentValidator();
Appointment appt = new Appointment(...);
validator.validate(...);
SubsystemARepository repoA = new SubsystemARepository(store);
repoA.save(appt);
// ... client must manually orchestrate B and C, and catch exceptions to undo previous steps