package com.telemed.subsystemA;

import com.telemed.domain.AppointmentStatus;
import com.telemed.exception.InvalidIntakeException;

import java.time.LocalDateTime;
import java.util.List;

public class SlotConflictChecker {

    public void ensureSlotIsFree(String doctorId, LocalDateTime slot, List<Appointment> existing) {
        if (slot == null) {
            throw new InvalidIntakeException("Appointment time is not specified");
        }
        if (slot.isBefore(LocalDateTime.now())) {
            throw new InvalidIntakeException("Cannot book a past time: " + slot);
        }
        for (Appointment a : existing) {
            if (a == null) continue;
            if (!a.getDoctorId().equals(doctorId) || !a.getSlot().equals(slot)) {
                continue;
            }
            if (a.getStatus() == AppointmentStatus.CANCELLED
                    || a.getStatus() == AppointmentStatus.COMPLETED) {
                continue;
            }
            throw new InvalidIntakeException(
                    "The slot " + slot + " is already booked for doctor " + doctorId);
        }
    }
}