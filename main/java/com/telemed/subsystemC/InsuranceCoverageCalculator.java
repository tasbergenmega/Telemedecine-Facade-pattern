package com.telemed.subsystemC;

public class InsuranceCoverageCalculator {

    public int patientShareCents(int baseAmountCents, int coveragePercent) {
        if (baseAmountCents < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (coveragePercent < 0 || coveragePercent > 100) {
            throw new IllegalArgumentException("coveragePercent must be in range 0..100");
        }
        int covered = baseAmountCents * coveragePercent / 100;
        return baseAmountCents - covered;
    }

    public int insuranceShareCents(int baseAmountCents, int coveragePercent) {
        return baseAmountCents - patientShareCents(baseAmountCents, coveragePercent);
    }
}