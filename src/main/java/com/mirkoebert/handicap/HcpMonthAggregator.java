package com.mirkoebert.handicap;

import com.mirkoebert.sgi.chart.HcpData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
class HcpMonthAggregator {

    static final String RANGE_LAST_TWO_YEARS = "lastTwoYears";
    static final String RANGE_ALL = "all";

    private static final int LAST_TWO_YEARS_MONTHS = 24;

    private final HcpRepository repo;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy");

    /**
     * Monthly handicap series. {@value #RANGE_ALL} runs from the earliest entry through the current
     * month. Anything else, including null, is the last 24 months.
     */
    HcpData getHcpForRange(String range, String userId) {
        Map<YearMonth, Double> byMonth = averagesByMonth(userId);
        if (RANGE_ALL.equalsIgnoreCase(range)) {
            return windowEndingNow(byMonth, monthsForAll(byMonth));
        }
        return windowEndingNow(byMonth, LAST_TWO_YEARS_MONTHS);
    }

    private Map<YearMonth, Double> averagesByMonth(String userId) {
        final List<HcpScoreEntity> allHcp = repo.findByUserId(userId);
        return allHcp
                .stream()
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getDate()),           // Group by Year + Month
                        Collectors.averagingDouble(HcpScoreEntity::getHcp)  // Average of amount
                ));
    }

    private int monthsForAll(Map<YearMonth, Double> byMonth) {
        if (byMonth.isEmpty()) {
            return LAST_TWO_YEARS_MONTHS;
        }
        YearMonth earliest = byMonth.keySet().stream().min(YearMonth::compareTo).orElseThrow();
        long span = ChronoUnit.MONTHS.between(earliest, YearMonth.now()) + 1;
        if (span < 1) {
            return LAST_TWO_YEARS_MONTHS;
        }
        return (int) span;
    }

    @SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "ConditionalBreakInInfiniteLoop"})
    HcpData getHcpForLastMonth(int i, String userId) {
        return windowEndingNow(averagesByMonth(userId), i);
    }

    @SuppressWarnings({"SequencedCollectionMethodCanBeUsed", "ConditionalBreakInInfiniteLoop"})
    private HcpData windowEndingNow(Map<YearMonth, Double> byMonth, int i) {
        final LocalDate now = LocalDate.now();
        final List<String> labels = new ArrayList<>(i);
        final List<Double> hcps = new ArrayList<>(i);

        for (int j = (i - 1); j >= 0; j--) {
            YearMonth mi = YearMonth.from(now.minusMonths(j));
            Double v = byMonth.get(mi);
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

    private boolean isNotEmpty(final List<Double> hcps) {
        return hcps.stream().anyMatch(Objects::nonNull);
    }

}
