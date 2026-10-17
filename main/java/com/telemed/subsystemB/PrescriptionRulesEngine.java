package com.telemed.subsystemB;

import com.telemed.exception.VideoCallFailedException;

import java.util.Locale;

public class PrescriptionRulesEngine {

    private static final int MIN_DOSAGE_MG = 5;
    private static final int MAX_DOSAGE_MG = 2000;

    public void ensurePrescriptionAllowed(String drugCode, int dosageMg) {
        if (drugCode == null || drugCode.isBlank()) {
            throw new VideoCallFailedException("Drug code is not specified");
        }
        if (dosageMg < MIN_DOSAGE_MG || dosageMg > MAX_DOSAGE_MG) {
            throw new VideoCallFailedException(
                    "Dosage " + dosageMg + " mg is outside the allowed range ["
                            + MIN_DOSAGE_MG + ", " + MAX_DOSAGE_MG + "]");
        }
    }

    public void ensureNoInteraction(String drugCode, String patientAllergies) {
        if (drugCode == null || drugCode.isBlank()) return;
        if (patientAllergies == null || patientAllergies.isBlank()) return;

        String normalizedDrug = normalize(drugCode);
        for (String allergy : patientAllergies.split("[;,\\s]+")) {
            if (!allergy.isBlank() && normalize(allergy).equals(normalizedDrug)) {
                throw new VideoCallFailedException(
                        "Patient has an allergy to medication " + drugCode);
            }
        }
    }

    private String normalize(String value) {
        return value.trim().replaceAll("[\\s_]+", "").toUpperCase(Locale.ROOT);
    }
}