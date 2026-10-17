package com.telemed.subsystemA;

import com.telemed.exception.InvalidIntakeException;

public class AppointmentValidator {

    public void validate(String doctorId, String patientId, String region,
                         boolean doctorLicensedInRegion) {
        if (doctorId == null || doctorId.isBlank()) {
            throw new InvalidIntakeException("Doctor is not specified");
        }
        if (patientId == null || patientId.isBlank()) {
            throw new InvalidIntakeException("Patient is not specified");
        }
        if (region == null || region.isBlank()) {
            throw new InvalidIntakeException("Patient region is not specified");
        }
        if (!doctorLicensedInRegion) {
            throw new InvalidIntakeException(
                    "Doctor " + doctorId + " is not licensed in region " + region);
        }
    }
}