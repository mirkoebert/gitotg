package com.mirkoebert.export;

import com.mirkoebert.user.CurrentUserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class CsvExportPrimaryRestController {

        private final HcpCsvExportService hcpCsvExportService;
        private final CurrentUserService currentUserService;

        @SneakyThrows
        @GetMapping("/api/handicap/export")
        public void getHcpCsv(@AuthenticationPrincipal final OAuth2User principal, final HttpServletResponse response){
                log.info("hcp export as csv");
                final String userId = currentUserService.getUserId(principal);
                String csv = hcpCsvExportService.exportAllHcpDataToCsv(userId);
                response.setContentType("text/csv");
                response.addHeader("Content-Disposition", "attachment; filename=\"handicap.csv\"");
                response.getOutputStream().print(csv);
        }


        @SneakyThrows
        @GetMapping("/api/sgi/export")
        public void getSgiCsv(@AuthenticationPrincipal final OAuth2User principal, final HttpServletResponse response){
                log.info("sgi export as csv");
                final String userId = currentUserService.getUserId(principal);
                String csv = hcpCsvExportService.exportAllSgiDataToCsv(userId);
                response.setContentType("text/csv");
                response.addHeader("Content-Disposition", "attachment; filename=\"handicap-short-game.csv\"");
                response.getOutputStream().print(csv);
        }

}

