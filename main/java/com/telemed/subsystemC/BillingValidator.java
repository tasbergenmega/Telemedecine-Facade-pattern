package com.telemed.subsystemC;

import com.telemed.exception.BillingRejectedException;

public class BillingValidator {

    public void ensureBillable(boolean hasValidPaymentMethod,
                               boolean insuranceApproved,
                               int patientShareCents) {
        if (!hasValidPaymentMethod) {
            throw new BillingRejectedException(
                    "The patient does not have a valid payment method");
        }
        if (!insuranceApproved && patientShareCents > 0) {
            throw new BillingRejectedException(
                    "Insurance is not approved; the patient must pay "
                            + patientShareCents + " cents");
        }
    }
}