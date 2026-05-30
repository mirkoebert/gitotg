package com.mirkoebert.sgi.chart;

import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SgiPrimaryRestController {

        private final MonthlySgiHcpAggregator monthlySgiHcpAggregator;
        private final CurrentUserService currentUserService;

        @GetMapping("/api/sgi/chart-data/{testId}")
        public ResponseEntity<HcpData> getLineChartData(@PathVariable String testId) {
                log.info("getLineChartData testId {}", testId);
                val u = currentUserService.getCurrentUser();
                final String userId = u.id();
                log.info("for user {}", userId);
                return ResponseEntity.ok(monthlySgiHcpAggregator.getHcpForLastMonth(12, userId, Integer.parseInt(testId)));
        }

}
