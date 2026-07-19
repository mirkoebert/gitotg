package com.mirkoebert.sgi.chart;

import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
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
public class MonthlySgiHcpAggregator {

        private final SingleTestResultRepository repo;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy");

        private Map<YearMonth, Double> getLastOfMonth(final String userId, final Integer testId) {
                final List<SingleTestResultEntity> allHcp = repo.findByUserIdAndTestId(userId, testId);
                return allHcp
                        .stream()
                        .collect(Collectors.groupingBy(
                                t -> YearMonth.from(t.getDate()),           // Group by Year + Month
                                Collectors.averagingDouble(SingleTestResultEntity::getHcp)  // Average of amount
                        ));
        }

        public HcpData getHcpForLastMonth(int i, String userId, final Integer testId) {
                // map it
                final LocalDate now = LocalDate.now();
                final List<String> labels = new ArrayList<>();
                final List<Double> hcps = new ArrayList<>();
                try {
                        Map<YearMonth, Double> ghcp = getLastOfMonth(userId, testId);



                        for (int j = (i - 1); j >= 0; j--) {
                                YearMonth mi = YearMonth.from(now.minusMonths(j));
                                Double v = ghcp.get(mi);
                                hcps.add(v);
                                labels.add(fmt.format(mi));
                        }

                        // front trim
                        while (true) {
                                if (hcps.isEmpty()) {
                                        log.info("No values");
                                        break;
                                }
                                if (hcps.getFirst() != null) {
                                        break;
                                }
                                hcps.removeFirst();
                                labels.removeFirst();
                        }
                } catch (Exception e) {
                    log.warn("Unexpected error", e);
                }
                return new HcpData(labels, hcps);
        }

}
