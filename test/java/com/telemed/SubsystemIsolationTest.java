package com.telemed;

import com.telemed.db.Store;
import com.telemed.domain.AppointmentStatus;
import com.telemed.subsystemA.Appointment;
import com.telemed.subsystemA.SubsystemARepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SubsystemIsolationTest {

    @Test
    void subsystemACanWorkStandaloneWithoutFacade(@TempDir Path tempDir) {
        Store store = new Store(tempDir.resolve("test.db").toString());
        SubsystemARepository repo = new SubsystemARepository(store);

        Appointment appt = new Appointment("DR-2", "PAT-2", LocalDateTime.now().plusHours(2), "US");
        appt.transitionTo(AppointmentStatus.CONFIRMED);
        repo.save(appt);

        Appointment loaded = repo.findById(appt.getId());
        assertNotNull(loaded);
        assertEquals(AppointmentStatus.CONFIRMED, loaded.getStatus());

        store.close();
    }
}