package com.mirkoebert.export;

import com.mirkoebert.user.CurrentUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CsvExportPrimaryRestController {

    private final HcpCsvExportService hcpCsvExportService;
    private final AllDataCsvExportService allDataCsvExportService;
    private final CurrentUserService currentUserService;
    private final CsvFileNameService csvFileNameService;
    private final CsvImportService csvImportService;
    private final MessageSource messageSource;

    @SneakyThrows
    @GetMapping("/api/handicap/export")
    public void getHcpCsv(final HttpServletResponse response) {
        log.info("hcp export as csv");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        String csv = hcpCsvExportService.exportAllHcpDataToCsv(userId);
        writeCsv(response, csvFileNameService.generateHcpExportFileName(), csv);
    }


    @SneakyThrows
    @GetMapping("/api/sgi/export")
    public void getSgiCsv(final HttpServletResponse response) {
        log.info("sgi export as csv");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        String csv = hcpCsvExportService.exportAllSgiDataToCsv(userId);
        writeCsv(response, csvFileNameService.generateSgiExportFileName(), csv);
    }

    @SneakyThrows
    @GetMapping("/api/gmetric/export")
    public void getGMetricCsv(final HttpServletResponse response) {
        log.info("gmetric export as csv");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        String csv = hcpCsvExportService.exportAllGMetricDataToCsv(userId);
        writeCsv(response, csvFileNameService.generateGMetricExportFileName(), csv);
    }

    @SneakyThrows
    @GetMapping("/api/export/all")
    public void getAllDataZip(final HttpServletResponse response) {
        log.info("export all data as zip");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        byte[] zip = allDataCsvExportService.exportAllDataAsZip(userId);
        response.setContentType("application/zip");
        attach(response, csvFileNameService.generateAllDataExportFileName());
        response.getOutputStream().write(zip);
    }

    @SneakyThrows
    @PostMapping(value = "/api/handicap/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importHcpCsv(@RequestParam("file") MultipartFile file) {
        log.info("hcp import csv");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        int count = csvImportService.importHcpData(file.getInputStream(), userId);
        return ResponseEntity.ok(messageSource.getMessage(
                "api.import.hcp", new Object[]{count}, LocaleContextHolder.getLocale()));
    }

    @SneakyThrows
    @PostMapping(value = "/api/sgi/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importSgiCsv(@RequestParam("file") MultipartFile file) {
        log.info("sgi import csv");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        int count = csvImportService.importSgiData(file.getInputStream(), userId);
        return ResponseEntity.ok(messageSource.getMessage(
                "api.import.sgi", new Object[]{count}, LocaleContextHolder.getLocale()));
    }

    @SneakyThrows
    @PostMapping(value = "/api/gmetric/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importGMetricCsv(@RequestParam("file") MultipartFile file) {
        log.info("gmetric import csv");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        int count = csvImportService.importGMetricData(file.getInputStream(), userId);
        return ResponseEntity.ok(messageSource.getMessage(
                "api.import.gmetric", new Object[]{count}, LocaleContextHolder.getLocale()));
    }

    /**
     * Writes the CSV as UTF-8 and says so in the content type. {@code ServletOutputStream.print}
     * would encode as ISO-8859-1 and reject anything above U+00FF.
     */
    private static void writeCsv(final HttpServletResponse response, final String filename, final String csv)
            throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding(StandardCharsets.UTF_8);
        attach(response, filename);
        response.getOutputStream().write(csv.getBytes(StandardCharsets.UTF_8));
    }

    private static void attach(final HttpServletResponse response, final String filename) {
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
    }
}
