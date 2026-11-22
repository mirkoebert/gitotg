package com.mirkoebert.golfmetric.consistency;

import com.mirkoebert.playedrounds.GolfCourseCatalog;
import com.mirkoebert.playedrounds.PlayedRoundEntity;
import com.mirkoebert.playedrounds.PlayedRoundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsistencyService {
        private static final int ROUND_WINDOW = 10;
        private final GolfCourseCatalog catalog;
        private final PlayedRoundRepository playedRoundRepository;
        private final ConsistencyCalculator consistencyCalculator;

        public Double getConsistency(final String userId) {
                try {
                        final String courseName = currentHomeCourseName(userId);
                        log.info("getConsistency {} {}", courseName, userId);
                        final List<Integer> totals = loadAllRoundsSorted(userId)
                                .stream()
                                .filter(round -> courseName.equals(round.getCourseName()))
                                .limit(ROUND_WINDOW)
                                .map(PlayedRoundEntity::getTotalStrokes)
                                .toList();
                        final Double result = consistencyCalculator.apply(totals);
                        log.info("getConsistency result {}", result);
                        return result;
                } catch (Exception e) {
                        log.info("Can't calc consistency {}", e.getMessage());
                        return null;
                }
        }

        public @NonNull Map<YearMonth, Double> getConsistencyByMonth(
                        final String userId,
                        final YearMonth start,
                        final YearMonth end
        ) {
                final Map<YearMonth, Double> result = new HashMap<>();
                try {
                        log.info("getConsistencyByMonth {} {} {}", userId, start, end);
                        final List<PlayedRoundEntity> allRounds = loadAllRoundsSorted(userId);
                        YearMonth cursor = start;
                        while (!cursor.isAfter(end)) {
                                Double value = consistencyAsOf(allRounds, cursor.atEndOfMonth());
                                if (value != null) {
                                        result.put(cursor, value);
                                }
                                cursor = cursor.plusMonths(1);
                        }
                        log.info("getConsistencyByMonth result {}", result.size());
                } catch (Exception e) {
                        log.info("Can't calc monthly consistency for {}: {}", userId, e.getMessage());
                }
                return result;
        }

        public @NonNull Map<Year, Double> getConsistencyByYear(final String userId, final Year start, final Year end) {
                final Map<Year, Double> result = new HashMap<>();
                try {
                        log.info("getConsistencyByYear {} {} {}", userId, start, end);
                        final List<PlayedRoundEntity> allRounds = loadAllRoundsSorted(userId);
                        final int years = (int) ChronoUnit.YEARS.between(start, end) + 1;
                        Year cursor = start;
                        for (int i = 0; i < years; i++) {
                                Double value = consistencyAsOf(allRounds, cursor.atMonth(12).atEndOfMonth());
                                if (value != null) {
                                        result.put(cursor, value);
                                }
                                cursor = cursor.plusYears(1);
                        }
                        log.info("getConsistencyByYear result {}", result.size());
                } catch (Exception e) {
                        log.info("Can't calc yearly consistency for {}: {}", userId, e.getMessage());
                }
                return result;
        }

        /**
         * Home course at {@code asOfInclusive} is the most-played course on or before that date.
         * Fewer than {@value #ROUND_WINDOW} rounds on that course → null.
         */
        private @Nullable Double consistencyAsOf(
                        final List<PlayedRoundEntity> allRoundsSortedDesc,
                        final LocalDate asOfInclusive
        ) {
                final String courseName = homeCourseNameAsOf(allRoundsSortedDesc, asOfInclusive);
                if (courseName == null) {
                        return null;
                }
                if (catalog.findByName(courseName).isEmpty()) {
                        return null;
                }
                final List<Integer> totals = allRoundsSortedDesc
                        .stream()
                        .filter(r -> courseName.equals(r.getCourseName()))
                        .filter(r -> !r.getDate().isAfter(asOfInclusive))
                        .limit(ROUND_WINDOW)
                        .map(PlayedRoundEntity::getTotalStrokes)
                        .toList();
                if (totals.size() < ROUND_WINDOW) {
                        return null;
                }
                return consistencyCalculator.apply(totals);
        }

        private @NonNull List<PlayedRoundEntity> loadAllRoundsSorted(final String userId) {
                return playedRoundRepository
                        .findByUserId(userId)
                        .stream()
                        .filter(r -> r.getDate() != null && r.getCourseName() != null)
                        .sorted(Comparator
                                .comparing(PlayedRoundEntity::getDate, Comparator.reverseOrder())
                                .thenComparing(PlayedRoundEntity::getId, Comparator.reverseOrder())
                        )
                        .toList();
        }

        private @Nullable String homeCourseNameAsOf(
                        final List<PlayedRoundEntity> allRounds,
                        final LocalDate asOfInclusive
        ) {
                final Map<String, Long> counts = allRounds
                        .stream()
                        .filter(r -> !r.getDate().isAfter(asOfInclusive))
                        .map(PlayedRoundEntity::getCourseName)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                return counts
                        .entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse(null);
        }

        private String currentHomeCourseName(final String userId) {
                List<PlayedRoundEntity> rounds = playedRoundRepository.findByUserId(userId);
                Map<String, Long> counts = rounds
                        .stream()
                        .map(PlayedRoundEntity::getCourseName)
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                Optional<Map.Entry<String, Long>> top = counts.entrySet().stream().max(Map.Entry.comparingByValue());
                log.info("top entry: {}", top);
                return top.get().getKey();
        }
}
