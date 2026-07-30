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

@RestController
@Slf4j
@RequiredArgsConstructor
public class CsvExportPrimaryRestController {

    private final HcpCsvExportService hcpCsvExportService;
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
        response.setContentType("text/csv");
        String filename = csvFileNameService.generateHcpExportFileName();
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().print(csv);
    }


    @SneakyThrows
    @GetMapping("/api/sgi/export")
    public void getSgiCsv(final HttpServletResponse response) {
        log.info("sgi export as csv");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        String csv = hcpCsvExportService.exportAllSgiDataToCsv(userId);
        response.setContentType("text/csv");
        String filename = csvFileNameService.generateSgiExportFileName();
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().print(csv);
    }

    @SneakyThrows
    @GetMapping("/api/gmetric/export")
    public void getGMetricCsv(final HttpServletResponse response) {
        log.info("gmetric export as csv");
        val u = currentUserService.getCurrentUser();
        final String userId = u.id();
        String csv = hcpCsvExportService.exportAllGMetricDataToCsv(userId);
        response.setContentType("text/csv");
        String filename = csvFileNameService.generateGMetricExportFileName();
        response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().print(csv);
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

}
