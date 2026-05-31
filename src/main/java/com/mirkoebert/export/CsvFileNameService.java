package com.mirkoebert.export;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Service responsible for generating consistent CSV export filenames
 * that include the current date.
 */
@Service
public class CsvFileNameService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generates a filename for handicap export including today's date.
     * Example: 2025-06-01-handicap.csv
     */
    public String generateHcpExportFileName() {
        return generateFileName("handicap");
    }

    /**
     * Generates a filename for Short Game Index (SGI) export including today's date.
     * Example: 2025-06-01-short-game.csv
     */
    public String generateSgiExportFileName() {
        return generateFileName("short-game");
    }

    /**
     * Generates a CSV filename with the given prefix and today's date.
     *
     * @param prefix the base name for the file (e.g. "handicap" or "short-game")
     * @return a filename in the format: YYYY-MM-DD-prefix.csv
     */
    public String generateFileName(String prefix) {
        return generateFileName(prefix, LocalDate.now());
    }

    /**
     * Generates a CSV filename with the given prefix and a specific date.
     * Useful for testing or backdating exports.
     */
    public String generateFileName(String prefix, LocalDate date) {
        if (prefix == null || prefix.isBlank()) {
            prefix = "export";
        }
        String formattedDate = date.format(DATE_FORMATTER);
        return formattedDate + "-" + prefix + ".csv";
    }
}
