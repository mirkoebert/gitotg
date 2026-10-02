package com.mirkoebert.handicap;

import com.mirkoebert.sgi.chart.HcpData;
import com.mirkoebert.user.CurrentUserService;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class HcpPrimaryRestController {

    private final HcpMonthAggregator monthlyHcpAggregator;
    private final CurrentUserService currentUserService;

    @GetMapping("/api/handicap/chart-data")
    public ResponseEntity<HcpData> getLineChartData(
            @RequestParam(defaultValue = HcpMonthAggregator.RANGE_LAST_TWO_YEARS) @Size(max = 88) String range
    ) {
        log.info("hcp getLineChartData range={}", range);
        val u = currentUserService.getCurrentUser();
        String userId = u.id();
        log.info("for user {}", userId);
        return ResponseEntity.ok(monthlyHcpAggregator.getHcpForRange(range, userId));
    }

}

