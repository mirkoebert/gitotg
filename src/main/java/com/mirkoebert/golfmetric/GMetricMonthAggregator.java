package com.mirkoebert.golfmetric;

import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
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
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
class GMetricMonthAggregator {

    public static final String RANGE_LAST_YEAR = "lastYear";
    public static final String RANGE_LAST_TWO_YEARS = "lastTwoYears";
    public static final String RANGE_ALL = "all";

    private static final int LAST_YEAR_MONTHS = 12;
    private static final int LAST_TWO_YEARS_MONTHS = 24;

    private final GMetricRepository repo;
    private final HcpRepository hcpRepository;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy");
    private final DateTimeFormatter fmtYear = DateTimeFormatter.ofPattern("yyyy");

    /**
     * Builds chart series for the given timeframe: monthly for
     * {@value #RANGE_LAST_YEAR} / {@value #RANGE_LAST_TWO_YEARS}, yearly for {@value #RANGE_ALL}.
     *
     * @param range {@value #RANGE_LAST_YEAR} (default), {@value #RANGE_LAST_TWO_YEARS}, or {@value #RANGE_ALL}
     */
    public @NonNull GMetricChartDataDto getMetricsForRange(final String range, final String userId) {
        if (RANGE_ALL.equalsIgnoreCase(range)) {
            return chartForAll(userId);
        }
        if (RANGE_LAST_TWO_YEARS.equalsIgnoreCase(range)) {
            return chartForLastMonths(userId, LAST_TWO_YEARS_MONTHS);
        }
        return chartForLastMonths(userId, LAST_YEAR_MONTHS);
    }

    private @NonNull GMetricChartDataDto chartForLastMonths(@NonNull final String userId, final int months) {
        final YearMonth end = YearMonth.now();
        final YearMonth start = end.minusMonths(months - 1L);
        // Keep a fixed month window so switching ranges is visible.
        return buildMonthlyChartData(userId, start, end);
    }

    private GMetricChartDataDto chartForAll(@NonNull final String userId) {
        final List<GMetricEntity> all = repo.findByUserId(userId);
        final List<HcpScoreEntity> handicaps = hcpRepository.findByUserId(userId);
        final LocalDate earliest = earliestDate(all, handicaps);
        if (earliest == null) {
            log.info("No metrics found for user {}", userId);
            return new GMetricChartDataDto(List.of(), List.of(), List.of(), List.of(), List.of());
        }
        final Year start = Year.from(earliest);
        final Year end = Year.now();
        return buildYearlyChartData(userId, start, end);
    }

    private LocalDate earliestDate(final List<GMetricEntity> metrics, final List<HcpScoreEntity> handicaps) {
        return Stream.concat(
                        metrics.stream().map(GMetricEntity::getDate),
                        handicaps.stream().map(HcpScoreEntity::getDate)
                )
                .filter(Objects::nonNull)
                .min(LocalDate::compareTo)
                .orElse(null);
    }

    private @NonNull GMetricChartDataDto buildMonthlyChartData(
            final String userId,
            final YearMonth start,
            final YearMonth end
    ) {
        final Map<GMetricType, Map<YearMonth, Double>> byType = new EnumMap<>(GMetricType.class);
        for (GMetricType type : GMetricType.values()) {
            byType.put(type, monthlyAveragesFrom(userId, type, start));
        }
        final Map<YearMonth, Double> handicapByMonth = handicapByMonth(userId, start);

        final int months = (int) ChronoUnit.MONTHS.between(start, end) + 1;
        final List<String> labels = new ArrayList<>(months);
        final List<Double> lostBalls = new ArrayList<>(months);
        final List<Double> doubleBogey = new ArrayList<>(months);
        final List<Double> bogey = new ArrayList<>(months);
        final List<Double> handicap = new ArrayList<>(months);

        YearMonth cursor = start;
        for (int i = 0; i < months; i++) {
            labels.add(fmt.format(cursor));
            lostBalls.add(oneDecimal(byType.get(GMetricType.LOST_BALLS_PER_9HOLES).get(cursor)));
            doubleBogey.add(oneDecimal(byType.get(GMetricType.DOUBLE_BOGEY_PLUS).get(cursor)));
            bogey.add(oneDecimal(byType.get(GMetricType.BOGEY_PLUS).get(cursor)));
            handicap.add(oneDecimal(handicapByMonth.get(cursor)));
            cursor = cursor.plusMonths(1);
        }

        log.debug("Chart for user {} months={} labels={}", userId, labels.size(), labels);
        return new GMetricChartDataDto(labels, lostBalls, doubleBogey, bogey, handicap);
    }

    private @NonNull GMetricChartDataDto buildYearlyChartData(
            final String userId,
            final Year start,
            final Year end
    ) {
        final Map<GMetricType, Map<Year, Double>> byType = new EnumMap<>(GMetricType.class);
        for (GMetricType type : GMetricType.values()) {
            byType.put(type, yearlyAveragesFrom(userId, type, start));
        }
        final Map<Year, Double> handicapByYear = handicapByYear(userId, start);

        final int years = (int) ChronoUnit.YEARS.between(start, end) + 1;
        final List<String> labels = new ArrayList<>(years);
        final List<Double> lostBalls = new ArrayList<>(years);
        final List<Double> doubleBogey = new ArrayList<>(years);
        final List<Double> bogey = new ArrayList<>(years);
        final List<Double> handicap = new ArrayList<>(years);

        Year cursor = start;
        for (int i = 0; i < years; i++) {
            labels.add(fmtYear.format(cursor));
            lostBalls.add(oneDecimal(byType.get(GMetricType.LOST_BALLS_PER_9HOLES).get(cursor)));
            doubleBogey.add(oneDecimal(byType.get(GMetricType.DOUBLE_BOGEY_PLUS).get(cursor)));
            bogey.add(oneDecimal(byType.get(GMetricType.BOGEY_PLUS).get(cursor)));
            handicap.add(oneDecimal(handicapByYear.get(cursor)));
            cursor = cursor.plusYears(1);
        }

        trimLeadingEmptyTimeBuckets(labels, lostBalls, doubleBogey, bogey, handicap);

        log.debug("Chart for user {} years={} labels={}", userId, labels.size(), labels);
        return new GMetricChartDataDto(labels, lostBalls, doubleBogey, bogey, handicap);
    }

    private Map<YearMonth, Double> monthlyAveragesFrom(final String userId, final GMetricType type, final YearMonth fromDateInclusive) {
        return repo.findByUserIdAndType(userId, type)
                .stream()
                .filter(m -> m.getDate() != null)
                .filter(m -> !YearMonth.from(m.getDate()).isBefore(fromDateInclusive))
                .collect(Collectors.groupingBy(
                        t -> YearMonth.from(t.getDate()),
                        Collectors.averagingDouble(GMetricEntity::getMetricValue)
                ));
    }

    private Map<YearMonth, Double> handicapByMonth(final String userId, final YearMonth fromDateInclusive) {
        return hcpRepository.findByUserId(userId)
                .stream()
                .filter(h -> h.getDate() != null && h.getHcp() != null)
                .filter(h -> !YearMonth.from(h.getDate()).isBefore(fromDateInclusive))
                .collect(Collectors.groupingBy(
                        h -> YearMonth.from(h.getDate()),
                        Collectors.averagingDouble(HcpScoreEntity::getHcp)
                ));
    }

    private Map<Year, Double> handicapByYear(final String userId, final Year fromDateInclusive) {
        return hcpRepository.findByUserId(userId)
                .stream()
                .filter(h -> h.getDate() != null && h.getHcp() != null)
                .filter(h -> !Year.from(h.getDate()).isBefore(fromDateInclusive))
                .collect(Collectors.groupingBy(
                        h -> Year.from(h.getDate()),
                        Collectors.averagingDouble(HcpScoreEntity::getHcp)
                ));
    }

    private static Double oneDecimal(final Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10.0) / 10.0;
    }

    private Map<Year, Double> yearlyAveragesFrom(String userId, GMetricType type, Year fromDateInclusive) {
        return repo.findByUserIdAndType(userId, type)
                .stream()
                .filter(m -> m.getDate() != null)
                .filter(m -> !Year.from(m.getDate()).isBefore(fromDateInclusive))
                .collect(Collectors.groupingBy(
                        t -> Year.from(t.getDate()),
                        Collectors.averagingDouble(GMetricEntity::getMetricValue)
                ));
    }

    private void trimLeadingEmptyTimeBuckets(
            List<String> labels,
            List<Double> lostBalls,
            List<Double> doubleBogey,
            List<Double> bogey,
            List<Double> handicap
    ) {
        while (!labels.isEmpty()
                && lostBalls.getFirst() == null
                && doubleBogey.getFirst() == null
                && bogey.getFirst() == null
                && handicap.getFirst() == null) {
            log.debug("Remove empty time bucket: {}", labels);
            labels.removeFirst();
            lostBalls.removeFirst();
            doubleBogey.removeFirst();
            bogey.removeFirst();
            handicap.removeFirst();
        }
    }
}
