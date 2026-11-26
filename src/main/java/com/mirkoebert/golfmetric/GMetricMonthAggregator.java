package com.mirkoebert.golfmetric;

import com.mirkoebert.golfmetric.consistency.ConsistencyService;
import com.mirkoebert.golfmetric.eclectic.EclecticService;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.playedrounds.PlayedRoundEntity;
import com.mirkoebert.playedrounds.PlayedRoundRepository;
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
        private final GMetricRepository repo;
        private final HcpRepository hcpRepository;
        private final EclecticService eclecticService;
        private final ConsistencyService consistencyService;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy");
        private final DateTimeFormatter fmtYear = DateTimeFormatter.ofPattern("yyyy");
        private final PlayedRoundRepository playedRoundRepository;

        /**
         * Builds chart series for the given timeframe. {@link GMetricRange#LAST_YEAR} and
         * {@link GMetricRange#LAST_TWO_YEARS} are monthly. {@link GMetricRange#ALL} is yearly.
         */
        public @NonNull GMetricChartDataDto getMetricsForRange(@NonNull final GMetricRange range, final String userId) {
                log.info("getMetricsForRange range={} user={}", range, userId);
                final List<PlayedRoundEntity> rounds = playedRoundRepository.findByUserId(userId);
                return switch (range) {
                        case ALL -> chartForAll(userId, rounds);
                        case LAST_YEAR, LAST_TWO_YEARS -> chartForLastMonths(userId, range.months(), rounds);
                };
        }

        private @NonNull GMetricChartDataDto chartForLastMonths(
                        @NonNull final String userId,
                        final int months,
                        final List<PlayedRoundEntity> rounds
        ) {
                final YearMonth end = YearMonth.now();
                final YearMonth start = end.minusMonths(months - 1L);
                // Keep a fixed month window so switching ranges is visible.
                return buildMonthlyChartData(userId, start, end, rounds);
        }

        private GMetricChartDataDto chartForAll(@NonNull final String userId, final List<PlayedRoundEntity> rounds) {
                final List<GMetricEntity> all = repo.findByUserId(userId);
                final List<HcpScoreEntity> handicaps = hcpRepository.findByUserId(userId);
                final LocalDate earliest = earliestDate(all, handicaps, rounds);
                if (earliest == null) {
                        log.info("No metrics found for user {}", userId);
                        return new GMetricChartDataDto(
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of()
                        );
                }
                final Year start = Year.from(earliest);
                final Year end = Year.now();
                return buildYearlyChartData(userId, start, end, rounds);
        }

        private LocalDate earliestDate(
                        final List<GMetricEntity> metrics,
                        final List<HcpScoreEntity> handicaps,
                        final List<PlayedRoundEntity> rounds
        ) {
                return Stream
                        .concat(
                                        Stream.concat(
                                                        metrics.stream().map(GMetricEntity::getDate),
                                                        handicaps.stream().map(HcpScoreEntity::getDate)
                                        ),
                                        rounds.stream().map(PlayedRoundEntity::getDate)
                        )
                        .filter(Objects::nonNull)
                        .min(LocalDate::compareTo)
                        .orElse(null);
        }

        private @NonNull GMetricChartDataDto buildMonthlyChartData(
                        final String userId,
                        final YearMonth start,
                        final YearMonth end,
                        final List<PlayedRoundEntity> rounds
        ) {
                final Map<GMetricType, Map<YearMonth, Double>> byType = new EnumMap<>(GMetricType.class);
                for (GMetricType type : GMetricType.values()) {
                        byType.put(type, monthlyAveragesFrom(userId, type, start));
                }
                final Map<YearMonth, Double> handicapByMonth = handicapByMonth(userId, start);
                final Map<YearMonth, Double> eclecticByMonth = eclecticService.getEclecticByMonth(userId, start, end);
                final Map<YearMonth, Double> consistencyByMonth =
                                consistencyService.getConsistencyByMonth(userId, start, end);
                final Map<YearMonth, Double> roundsByMonth = playedRoundsByMonth(rounds, start, end);

                final int months = (int) ChronoUnit.MONTHS.between(start, end) + 1;
                final List<String> labels = new ArrayList<>(months);
                final List<Double> lostBalls = new ArrayList<>(months);
                final List<Double> doubleBogey = new ArrayList<>(months);
                final List<Double> bogey = new ArrayList<>(months);
                final List<Double> handicap = new ArrayList<>(months);
                final List<Double> eclectic = new ArrayList<>(months);
                final List<Double> consistency = new ArrayList<>(months);
                final List<Double> playedRounds = new ArrayList<>(months);

                YearMonth cursor = start;
                for (int i = 0; i < months; i++) {
                        labels.add(fmt.format(cursor));
                        lostBalls.add(oneDecimal(byType.get(GMetricType.LOST_BALLS_PER_9HOLES).get(cursor)));
                        doubleBogey.add(oneDecimal(byType.get(GMetricType.DOUBLE_BOGEY_PLUS).get(cursor)));
                        bogey.add(oneDecimal(byType.get(GMetricType.BOGEY_PLUS).get(cursor)));
                        handicap.add(oneDecimal(handicapByMonth.get(cursor)));
                        eclectic.add(oneDecimal(eclecticByMonth.get(cursor)));
                        consistency.add(oneDecimal(consistencyByMonth.get(cursor)));
                        playedRounds.add(oneDecimal(roundsByMonth.getOrDefault(cursor, 0.0)));
                        cursor = cursor.plusMonths(1);
                }

                log.debug("Chart for user {} months={} labels={}", userId, labels.size(), labels);
                return new GMetricChartDataDto(
                                labels,
                                lostBalls,
                                doubleBogey,
                                bogey,
                                handicap,
                                eclectic,
                                consistency,
                                playedRounds
                );
        }

        private @NonNull GMetricChartDataDto buildYearlyChartData(
                        final String userId,
                        final Year start,
                        final Year end,
                        final List<PlayedRoundEntity> rounds
        ) {
                final Map<GMetricType, Map<Year, Double>> byType = new EnumMap<>(GMetricType.class);
                for (GMetricType type : GMetricType.values()) {
                        byType.put(type, yearlyAveragesFrom(userId, type, start));
                }
                final Map<Year, Double> handicapByYear = handicapByYear(userId, start);
                final Map<Year, Double> eclecticByYear = eclecticService.getEclecticByYear(userId, start, end);
                final Map<Year, Double> consistencyByYear = consistencyService.getConsistencyByYear(userId, start, end);
                final Map<Year, Double> roundsByYear = playedRoundsByYear(rounds, start, end);

                final int years = (int) ChronoUnit.YEARS.between(start, end) + 1;
                final List<String> labels = new ArrayList<>(years);
                final List<Double> lostBalls = new ArrayList<>(years);
                final List<Double> doubleBogey = new ArrayList<>(years);
                final List<Double> bogey = new ArrayList<>(years);
                final List<Double> handicap = new ArrayList<>(years);
                final List<Double> eclectic = new ArrayList<>(years);
                final List<Double> consistency = new ArrayList<>(years);
                final List<Double> playedRounds = new ArrayList<>(years);

                Year cursor = start;
                for (int i = 0; i < years; i++) {
                        labels.add(fmtYear.format(cursor));
                        lostBalls.add(oneDecimal(byType.get(GMetricType.LOST_BALLS_PER_9HOLES).get(cursor)));
                        doubleBogey.add(oneDecimal(byType.get(GMetricType.DOUBLE_BOGEY_PLUS).get(cursor)));
                        bogey.add(oneDecimal(byType.get(GMetricType.BOGEY_PLUS).get(cursor)));
                        handicap.add(oneDecimal(handicapByYear.get(cursor)));
                        eclectic.add(oneDecimal(eclecticByYear.get(cursor)));
                        consistency.add(oneDecimal(consistencyByYear.get(cursor)));
                        playedRounds.add(oneDecimal(roundsByYear.getOrDefault(cursor, 0.0)));
                        cursor = cursor.plusYears(1);
                }

                trimLeadingEmptyTimeBuckets(
                                labels,
                                lostBalls,
                                doubleBogey,
                                bogey,
                                handicap,
                                eclectic,
                                consistency,
                                playedRounds
                );

                log.debug("Chart for user {} years={} labels={}", userId, labels.size(), labels);
                return new GMetricChartDataDto(
                                labels,
                                lostBalls,
                                doubleBogey,
                                bogey,
                                handicap,
                                eclectic,
                                consistency,
                                playedRounds
                );
        }

        private Map<YearMonth, Double> playedRoundsByMonth(
                        final List<PlayedRoundEntity> rounds,
                        final YearMonth start,
                        final YearMonth end
        ) {
                return rounds
                        .stream()
                        .map(PlayedRoundEntity::getDate)
                        .filter(Objects::nonNull)
                        .map(YearMonth::from)
                        .filter(month -> !month.isBefore(start) && !month.isAfter(end))
                        .collect(Collectors.groupingBy(month -> month, Collectors.summingDouble(month -> 1.0)));
        }

        private Map<Year, Double> playedRoundsByYear(
                        final List<PlayedRoundEntity> rounds,
                        final Year start,
                        final Year end
        ) {
                return rounds
                        .stream()
                        .map(PlayedRoundEntity::getDate)
                        .filter(Objects::nonNull)
                        .map(Year::from)
                        .filter(year -> !year.isBefore(start) && !year.isAfter(end))
                        .collect(Collectors.groupingBy(year -> year, Collectors.summingDouble(year -> 1.0)));
        }

        private Map<YearMonth, Double> monthlyAveragesFrom(
                        final String userId,
                        final GMetricType type,
                        final YearMonth fromDateInclusive
        ) {
                return repo
                        .findByUserIdAndType(userId, type)
                        .stream()
                        .filter(m -> m.getDate() != null)
                        .filter(m -> !YearMonth.from(m.getDate()).isBefore(fromDateInclusive))
                        .collect(Collectors.groupingBy(
                                        t -> YearMonth.from(t.getDate()),
                                        Collectors.averagingDouble(GMetricEntity::getMetricValue)
                        ));
        }

        private Map<YearMonth, Double> handicapByMonth(final String userId, final YearMonth fromDateInclusive) {
                return hcpRepository
                        .findByUserId(userId)
                        .stream()
                        .filter(h -> h.getDate() != null && h.getHcp() != null)
                        .filter(h -> !YearMonth.from(h.getDate()).isBefore(fromDateInclusive))
                        .collect(Collectors.groupingBy(
                                        h -> YearMonth.from(h.getDate()),
                                        Collectors.averagingDouble(HcpScoreEntity::getHcp)
                        ));
        }

        private Map<Year, Double> handicapByYear(final String userId, final Year fromDateInclusive) {
                return hcpRepository
                        .findByUserId(userId)
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
                return repo
                        .findByUserIdAndType(userId, type)
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
                        List<Double> handicap,
                        List<Double> eclectic,
                        List<Double> consistency,
                        List<Double> playedRounds
        ) {
                while (!labels.isEmpty()
                                && lostBalls.getFirst() == null
                                && doubleBogey.getFirst() == null
                                && bogey.getFirst() == null
                                && handicap.getFirst() == null
                                && eclectic.getFirst() == null
                                && consistency.getFirst() == null
                                && playedRounds.getFirst() == null) {
                        log.debug("Remove empty time bucket: {}", labels);
                        labels.removeFirst();
                        lostBalls.removeFirst();
                        doubleBogey.removeFirst();
                        bogey.removeFirst();
                        handicap.removeFirst();
                        eclectic.removeFirst();
                        consistency.removeFirst();
                        playedRounds.removeFirst();
                }
        }
}
