package com.mirkoebert.export;

/**
 * Thrown when a CSV import exceeds {@link CsvImportService#MAX_CSV_LINES} records
 * (header plus data rows). Existing user data must not be deleted when this is thrown.
 */
public class CsvImportTooManyLinesException extends RuntimeException {

    private final int maxLines;

    public CsvImportTooManyLinesException(int maxLines) {
        super("CSV import exceeds the maximum of " + maxLines + " lines");
        this.maxLines = maxLines;
    }

    public int getMaxLines() {
        return maxLines;
    }
}
