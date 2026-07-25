package com.mirkoebert.golfmetric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GMetricMonthAggregator {

        private final GMetricRepository repo;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy");

        private Map<YearMonth, Double> monthlyAverages(String userId, GMetricType type) {
                final List<GMetricEntity> metrics = repo.findByUserIdAndType(userId, type);
                return metrics
                                .stream()
                                .collect(Collectors.groupingBy(
                                                t -> YearMonth.from(t.getDate()),
                                                Collectors.averagingDouble(GMetricEntity::getMetricValue)
                                ));
        }

        public @NonNull GMetricChartData getMetricsForLastMonths(int months, String userId) {
                Map<GMetricType, Map<YearMonth, Double>> byType = new EnumMap<>(GMetricType.class);
                for (GMetricType type : GMetricType.values()) {
                        byType.put(type, monthlyAverages(userId, type));
                }

                final LocalDate now = LocalDate.now();
                final List<String> labels = new ArrayList<>(months);
                final List<Double> lostBalls = new ArrayList<>(months);
                final List<Double> doubleBogey = new ArrayList<>(months);
                final List<Double> bogey = new ArrayList<>(months);

                for (int j = months - 1; j >= 0; j--) {
                        YearMonth mi = YearMonth.from(now.minusMonths(j));
                        labels.add(fmt.format(mi));
                        lostBalls.add(byType.get(GMetricType.LOST_BALLS).get(mi));
                        doubleBogey.add(byType.get(GMetricType.DOUBLE_BOGEY).get(mi));
                        bogey.add(byType.get(GMetricType.BOGEY).get(mi));
                }

                trimLeadingEmptyMonths(labels, lostBalls, doubleBogey, bogey);

                return new GMetricChartData(labels, lostBalls, doubleBogey, bogey);
        }

        private void trimLeadingEmptyMonths(
                        List<String> labels,
                        List<Double> lostBalls,
                        List<Double> doubleBogey,
                        List<Double> bogey) {
                while (!labels.isEmpty()
                                && lostBalls.getFirst() == null
                                && doubleBogey.getFirst() == null
                                && bogey.getFirst() == null) {
                        labels.removeFirst();
                        lostBalls.removeFirst();
                        doubleBogey.removeFirst();
                        bogey.removeFirst();
                }

                // Keep at least empty series if everything was null (no data at all)
                if (labels.isEmpty()) {
                        return;
                }

                // If all series are fully empty (only trailing months with data path), leave as-is.
                // Caller already has months from first non-null across any series.
                if (lostBalls.stream().noneMatch(Objects::nonNull)
                                && doubleBogey.stream().noneMatch(Objects::nonNull)
                                && bogey.stream().noneMatch(Objects::nonNull)) {
                        log.debug("No gmetric values for chart");
                }
        }
}
