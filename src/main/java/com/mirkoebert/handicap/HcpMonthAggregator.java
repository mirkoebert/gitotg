package com.mirkoebert.handicap;

import com.mirkoebert.sgi.chart.HcpData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
class HcpMonthAggregator {

        private final HcpRepository repo;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy");

        private Map<YearMonth, Double> getLastOfMonth(String userId) {
                final List<HcpScoreEntity> allHcp = repo.findByUserId(userId);
                return allHcp
                        .stream()
                        .collect(Collectors.groupingBy(
                                t -> YearMonth.from(t.getDate()),           // Group by Year + Month
                                Collectors.averagingDouble(HcpScoreEntity::getHcp)  // Average of amount
                        ));
        }

        HcpData getHcpForLastMonth(int i, String userId) {
                Map<YearMonth, Double> ghcp = getLastOfMonth(userId);

                // map it
                final LocalDate now = LocalDate.now();
                final List<String> labels = new ArrayList<>(i);
                final List<Double> hcps = new ArrayList<>(i);

                for (int j = (i - 1); j >= 0; j--) {
                        YearMonth mi = YearMonth.from(now.minusMonths(j));
                        Double v = ghcp.get(mi);
                        hcps.add(v);
                        labels.add(fmt.format(mi));
                }

                if (isNotEmpty(hcps)) {
                        // front trim
                        while (true) {
                                if (hcps.get(0) != null) {
                                        break;
                                }
                                hcps.remove(0);
                                labels.remove(0);
                        }
                }

                return new HcpData(labels, hcps);
        }

        private boolean isNotEmpty(List<Double> hcps) {
                boolean result = false;
                result = hcps.stream().anyMatch(hcp -> hcp != null);
                return result;
        }

}
