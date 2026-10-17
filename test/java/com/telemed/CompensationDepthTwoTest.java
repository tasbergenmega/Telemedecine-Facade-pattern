package com.telemed;

import com.telemed.dsl.TelemedScenario;
import com.telemed.exception.BillingRejectedException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

class CompensationDepthTwoTest {

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
    @DisplayName("Если биллинг (C) отклонен, фасад откатывает B, а затем A")
    void cFailsAfterAAndB_compensatesBThenA() {
        // Given
        scenario.givenRejectedInsurance();

        // When
        assertThrows(BillingRejectedException.class, scenario::whenBookingConsultation);

        // Then
        assertEquals("COMPENSATED", scenario.chainStatus());
        assertEquals("CANCELLED", scenario.appointmentStatus(), "A должен быть откачен");
        assertEquals("FAILED", scenario.sessionStatus(), "B должен быть откачен");
        assertEquals("VOIDED", scenario.prescriptionStatus(), "Рецепт B должен быть аннулирован");
    }
}