package com.mirkoebert.handicap;

import com.mirkoebert.sgi.chart.HcpData;
import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HcpPrimaryRestController {

        private final HcpMonthAggregator monthlyHcpAggregator;
        private final CurrentUserService currentUserService;

        @GetMapping("/api/handicap/chart-data")
        public ResponseEntity<HcpData> getLineChartData() {
                log.info("hcp getLineChartData");
                val u = currentUserService.getCurrentUser();
                String userId = u.id();
                log.info("for user {}", userId);
                return ResponseEntity.ok(monthlyHcpAggregator.getHcpForLastMonth(36, userId));
        }

}

