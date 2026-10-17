package com.telemed;

import com.telemed.db.Store;
import com.telemed.dsl.TelemedScenario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PersistenceReloadTest {

    @Test
    void shouldResumeFromDurableStateAfterAppRestart(@TempDir Path tempDir) {
        String dbPath = tempDir.resolve("test.db").toString();

        // 1. Запускаем сценарий (успешный)
        TelemedScenario firstRun = new TelemedScenario(dbPath);
        firstRun.whenBookingConsultation();
        String chainId = firstRun.lastChainId();
        assertEquals("COMPLETED", firstRun.chainStatus());

        // 2. Закрываем приложение (симуляция остановки сервера)
        firstRun.close();

        // 3. Открываем "заново", передавая тот же файл БД
        TelemedScenario secondRun = new TelemedScenario(dbPath);

        // 4. Проверяем, что фасад прочитал статус с диска, а не из памяти
        assertEquals("COMPLETED", secondRun.orchestrator.readStatus(chainId).name());

        secondRun.close();
    }
}