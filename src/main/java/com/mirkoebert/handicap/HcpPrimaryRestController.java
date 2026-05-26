package com.mirkoebert.handicap;

import com.mirkoebert.sgi.chart.HcpData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HcpPrimaryRestController {

        private final HcpMonthAggregator monthlyHcpAggregator;

        @GetMapping("/api/handicap/chart-data")
        public ResponseEntity<HcpData> getLineChartData(@AuthenticationPrincipal final OAuth2User principal) {
                log.info("hcp getLineChartData");
                String userId = (String) principal.getAttributes().get("sub");
                log.info("for user {}", userId);
                return ResponseEntity.ok(monthlyHcpAggregator.getHcpForLastMonth(36, userId));
        }

}

