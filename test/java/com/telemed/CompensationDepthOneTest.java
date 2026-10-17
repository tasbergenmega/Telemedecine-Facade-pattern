package com.telemed;

import com.telemed.dsl.TelemedScenario;
import com.telemed.exception.VideoCallFailedException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CompensationDepthOneTest {

    private TelemedScenario scenario;

    @BeforeEach
    void setUp() {
        scenario = new TelemedScenario(":memory:");
    }

    @AfterEach
    void tearDown() {
        scenario.close();
    }

    @Test
    @DisplayName("Если видеозвонок (B) не удался, фасад откатывает ТОЛЬКО запись (A)")
    void bFailsAfterA_compensatesOnlyA() {
        scenario.givenOfflineNetwork();

        assertThrows(VideoCallFailedException.class, scenario::whenBookingConsultation);
        assertEquals("COMPENSATED", scenario.chainStatus());
        assertEquals("CANCELLED", scenario.appointmentStatus());
        assertNull(scenario.sessionStatus(), "Видеосессия не должна была создаться");
        assertNull(scenario.prescriptionStatus(), "Рецепт не должен был создаться");
    }

    @Test
    @DisplayName("Неизвестный networkId должен падать как ошибка B, а не проходить как хороший интернет")
    void unknownNetworkId_isRejected() {
        scenario.network = "1";

        assertThrows(VideoCallFailedException.class, scenario::whenBookingConsultation);
        assertEquals("COMPENSATED", scenario.chainStatus());
        assertEquals("CANCELLED", scenario.appointmentStatus());
    }
}