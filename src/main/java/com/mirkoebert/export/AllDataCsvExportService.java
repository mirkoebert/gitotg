package com.mirkoebert.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AllDataCsvExportService {

    private final HcpCsvExportService hcpCsvExportService;
    private final CsvFileNameService csvFileNameService;

    byte[] exportAllDataAsZip(final String userId) {
        val baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            addCsvEntry(zos, csvFileNameService.generateHcpExportFileName(), hcpCsvExportService.exportAllHcpDataToCsv(userId));
            addCsvEntry(zos, csvFileNameService.generateSgiExportFileName(), hcpCsvExportService.exportAllSgiDataToCsv(userId));
            addCsvEntry(zos, csvFileNameService.generateGMetricExportFileName(), hcpCsvExportService.exportAllGMetricDataToCsv(userId));
        } catch (IOException e) {
            log.error("Can't generate ZIP export for user {}", userId, e);
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private void addCsvEntry(final ZipOutputStream zos, final String fileName, final String csv) throws IOException {
        zos.putNextEntry(new ZipEntry(fileName));
        zos.write(csv.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }
}
