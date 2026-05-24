package com.mirkoebert.sgi;

import com.mirkoebert.sgi.aggregator.HcpData;
import com.mirkoebert.sgi.aggregator.MonthlySgiHcpAggregator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SgiPrimaryRestController {

        private final MonthlySgiHcpAggregator monthlySgiHcpAggregator;

        @GetMapping("/api/sgi/chart-data/{testId}")
        public ResponseEntity<HcpData> getLineChartData(@AuthenticationPrincipal OAuth2User principal, @PathVariable String testId) {
                log.info("getLineChartData testId {}", testId);
                String userId = (String) principal.getAttributes().get("sub");
                log.info("for user {}", userId);
                return ResponseEntity.ok(monthlySgiHcpAggregator.getHcpForLastMonth(12, userId, Integer.parseInt(testId)));
        }

}

