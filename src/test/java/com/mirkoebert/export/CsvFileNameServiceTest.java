package com.mirkoebert.export;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CsvFileNameServiceTest {

    private final CsvFileNameService service = new CsvFileNameService();

    @Test
    void generatesHcpFilenameWithTodayDate() {
        String filename = service.generateHcpExportFileName();
        assertThat(filename)
                .startsWith(LocalDate.now().toString())
                .endsWith("-handicap.csv");
    }

    @Test
    void generatesSgiFilenameWithTodayDate() {
        String filename = service.generateSgiExportFileName();
        assertThat(filename)
                .startsWith(LocalDate.now().toString())
                .endsWith("-short-game.csv");
    }

    @Test
    void generatesCustomFilenameWithDate() {
        LocalDate date = LocalDate.of(2025, 5, 15);
        String filename = service.generateFileName("my-export", date);
        assertThat(filename).isEqualTo("2025-05-15-my-export.csv");
    }

    @Test
    void fallsBackToExportPrefixWhenNull() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        String filename = service.generateFileName(null, date);
        assertThat(filename).isEqualTo("2025-01-01-export.csv");
    }
}
