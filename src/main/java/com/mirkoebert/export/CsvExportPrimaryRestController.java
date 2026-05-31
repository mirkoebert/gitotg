package com.mirkoebert.export;

import com.mirkoebert.user.CurrentUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CsvExportPrimaryRestController {

        private final HcpCsvExportService hcpCsvExportService;
        private final CurrentUserService currentUserService;
        private final CsvFileNameService csvFileNameService;

        @SneakyThrows
        @GetMapping("/api/handicap/export")
        public void getHcpCsv(final HttpServletResponse response){
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
        public void getSgiCsv(final HttpServletResponse response){
                log.info("sgi export as csv");
                val u = currentUserService.getCurrentUser();
                final String userId = u.id();
                String csv = hcpCsvExportService.exportAllSgiDataToCsv(userId);
                response.setContentType("text/csv");
                String filename = csvFileNameService.generateSgiExportFileName();
                response.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                response.getOutputStream().print(csv);
        }

}
