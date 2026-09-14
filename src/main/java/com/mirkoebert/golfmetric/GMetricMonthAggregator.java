package com.mirkoebert.golfmetric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GMetricMonthAggregator {

    public static final String RANGE_LAST_YEAR = "lastYear";
    public static final String RANGE_ALL = "all";

    private static final int LAST_YEAR_MONTHS = 12;

    private final GMetricRepository repo;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy");
    private final DateTimeFormatter fmtYear = DateTimeFormatter.ofPattern("yyyy");

    /**
     * Builds monthly chart series for the given timeframe.
     *
     * @param range {@value #RANGE_LAST_YEAR} (default) or {@value #RANGE_ALL}
     */
    public @NonNull GMetricChartDataDto getMetricsForRange(final String range, final String userId) {
        if (RANGE_ALL.equalsIgnoreCase(range)) {
            return chartForAll(userId);
        }
        return chartForLastYear(userId);
    }

    private @NonNull GMetricChartDataDto chartForLastYear(@NonNull final String userId) {
        final YearMonth end = YearMonth.now();
        final YearMonth start = end.minusMonths(LAST_YEAR_MONTHS - 1L);
        // Keep a fixed 12-month window so switching ranges is visible.
        return buildMonthlyChartData(userId, start, end, false);
    }

    private GMetricChartDataDto chartForAll(@NonNull final String userId) {
        final List<GMetricEntity> all = repo.findByUserId(userId);
        if (all.isEmpty()) {
            log.info("No metrics found for user {}", userId);
            return emptyMetricsForLast3Years();
        }
        final LocalDate earliest = all
                .stream()
                .map(GMetricEntity::getDate)
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .get();
        final Year start = Year.from(earliest);
        final Year end = Year.now();
        return buildYearlyChartData(userId, start, end, true);
    }

    private GMetricChartDataDto emptyMetricsForLast3Years() {
        final Year end = Year.now();
        final Year start = Year.from(end).minusYears(3);

        final List<String> labels = new ArrayList<>();
        final List<Double> lostBalls = new ArrayList<>();
        final List<Double> doubleBogey = new ArrayList<>();
        final List<Double> bogey = new ArrayList<>();

        Year cursor = start;
        for (int i = 0; i < 3; i++) {
            labels.add(fmtYear.format(cursor));
            lostBalls.add(0.0);
            doubleBogey.add(0.0);
            bogey.add(0.0);
            cursor = cursor.plusYears(1);
        }
        return new GMetricChartDataDto(labels, lostBalls, doubleBogey, bogey);
    }

    /**
     * @param trimLeading when true, drop empty months before the first value (useful for "all")
     */
    private @NonNull GMetricChartDataDto buildMonthlyChartData(
            final String userId,
            YearMonth start,
            YearMonth end,
            boolean trimLeading
    ) {
        final LocalDate fromDate = start.atDay(1);
        final Map<GMetricType, Map<YearMonth, Double>> byType = new EnumMap<>(GMetricType.class);
        for (GMetricType type : GMetricType.values()) {
            byType.put(type, monthlyAveragesFrom(userId, type, fromDate));
        }

        final int months = (int) ChronoUnit.MONTHS.between(start, end) + 1;
        final List<String> labels = new ArrayList<>(months);
        final List<Double> lostBalls = new ArrayList<>(months);
        final List<Double> doubleBogey = new ArrayList<>(months);
        final List<Double> bogey = new ArrayList<>(months);

        YearMonth cursor = start;
        for (int i = 0; i < months; i++) {
            labels.add(fmt.format(cursor));
            lostBalls.add(byType.get(GMetricType.LOST_BALLS).get(cursor));
            doubleBogey.add(byType.get(GMetricType.DOUBLE_BOGEY_PLUS).get(cursor));
            bogey.add(byType.get(GMetricType.BOGEY_PLUS).get(cursor));
            cursor = cursor.plusMonths(1);
        }

        if (trimLeading) {
            trimLeadingEmptyMonths(labels, lostBalls, doubleBogey, bogey);
        }

        log.debug("Chart for user {} months={} labels={}", userId, labels.size(), labels.size());
        return new GMetricChartDataDto(labels, lostBalls, doubleBogey, bogey);
    }

    private @NonNull GMetricChartDataDto buildYearlyChartData(
            final String userId,
            final Year start,
            final Year end,
            boolean trimLeading
    ) {
        final LocalDate fromDate = start.atDay(1);
        final Map<GMetricType, Map<Year, Double>> byType = new EnumMap<>(GMetricType.class);
        for (GMetricType type : GMetricType.values()) {
            byType.put(type, yearlyAveragesFrom(userId, type, fromDate));
        }

        final int years = (int) ChronoUnit.YEARS.between(start, end) + 1;
        final List<String> labels = new ArrayList<>(years);
        final List<Double> lostBalls = new ArrayList<>(years);
        final List<Double> doubleBogey = new ArrayList<>(years);
        final List<Double> bogey = new ArrayList<>(years);

        Year cursor = start;
        for (int i = 0; i < years; i++) {
            labels.add(fmtYear.format(cursor));
            lostBalls.add(byType.get(GMetricType.LOST_BALLS).get(cursor));
            doubleBogey.add(byType.get(GMetricType.DOUBLE_BOGEY_PLUS).get(cursor));
            bogey.add(byType.get(GMetricType.BOGEY_PLUS).get(cursor));
            cursor = cursor.plusYears(1);
        }

        if (trimLeading) {
            trimLeadingEmptyMonths(labels, lostBalls, doubleBogey, bogey);
        }

        log.debug("Chart for user {} years={} labels={}", userId, labels.size(), labels.size());
        return new GMetricChartDataDto(labels, lostBalls, doubleBogey, bogey);
    }

    private Map<YearMonth, Double> monthlyAveragesFrom(String userId, GMetricType type, LocalDate fromDateInclusive) {
        return repo.findByUserIdAndType(userId, type)
                .stream()
                .filter(m -> m.getDate() != null && !m.getDate().isBefore(fromDateInclusive))
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getDate()),
                        Collectors.averagingDouble(GMetricEntity::getMetricValue)
                ));
    }

    private Map<Year, Double> yearlyAveragesFrom(String userId, GMetricType type, LocalDate fromDateInclusive) {
        return repo.findByUserIdAndType(userId, type)
                .stream()
                .filter(m -> m.getDate() != null && !m.getDate().isBefore(fromDateInclusive))
                .collect(Collectors.groupingBy(
                        t -> Year.from(t.getDate()),
                        Collectors.averagingDouble(GMetricEntity::getMetricValue)
                ));
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
    }
}
