package com.mirkoebert.golfmetric;

import com.mirkoebert.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class GMetricRestController {

        private final GMetricService gMetricService;
        private final GMetricMonthAggregator monthAggregator;
        private final CurrentUserService currentUserService;

        @GetMapping("/api/gmetric")
        public ResponseEntity<List<GMetricEntity>> getGMetrics(
                        @RequestParam(required = false) GMetricType type) {
                val u = currentUserService.getCurrentUser();
                final String userId = u.id();
                log.info("getGMetrics for user {} type {}", userId, type);

                final List<GMetricEntity> metrics = type == null
                                ? gMetricService.findByUserId(userId)
                                : gMetricService.findByUserIdAndType(userId, type);
                return ResponseEntity.ok(metrics);
        }

        @GetMapping("/api/gmetric/chart-data")
        public ResponseEntity<GMetricChartData> getChartData() {
                log.info("gmetric getChartData");
                val u = currentUserService.getCurrentUser();
                final String userId = u.id();
                log.info("for user {}", userId);
                return ResponseEntity.ok(monthAggregator.getMetricsForLastMonths(36, userId));
        }
}
