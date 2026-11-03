package com.mirkoebert.golfmetric.eclectic;

import com.mirkoebert.golfcourse.*;
import com.mirkoebert.handicap.HcpScoreDTO;
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
public class EclecticService {

    private static final int ECLECTIC_ROUND_WINDOW = 10;

    private final GolfCourseCatalog catalog;
    private final PlayedRoundRepository playedRoundRepository;


    Double getEclecticHcpForYear(int year, final String userId){
        try {
            final String courseName = getHomeCourseName(userId);
            log.info("getEclecticHcpForYear {} {} {}", year, courseName, userId);
            final GolfCourse c = catalog.findByName(courseName).get();

            List<PlayedRoundEntity> allPlayedRounds = playedRoundRepository.findAllByUserIdAndCourseName(userId, courseName);
            List<PlayedRoundEntity> playedRoundsOfYear = allPlayedRounds
                    .stream()
                    .filter(x -> x.getDate().getYear() == year)
                    .toList();
            int countOfHoles = c.getHoles().size();
            int eclecticSum = 0;
            for (int i = 0; i < countOfHoles; i++) {
                eclecticSum = eclecticSum + getBestShotCountForHole(i, playedRoundsOfYear);
            }
            log.info("eclecticSum {}", eclecticSum);


            return (double) eclecticSum;
        } catch (Exception e) {
            log.info("Can't calc eclectic hcp {}", e.getMessage());
            return null;
        }
    }

    public Integer getEclecticHcpForLast10Rounds(final String userId){
        try {
            final String courseName = getHomeCourseName(userId);
            log.info("getEclecticHcpForLast10Rounds {} {}",  courseName, userId);
            final GolfCourse c = catalog.findByName(courseName).get();

            List<PlayedRoundEntity> allPlayedRounds = playedRoundRepository.findAllByUserIdAndCourseName(userId, courseName);
            List<PlayedRoundEntity> playedRoundsSelected = allPlayedRounds
                    .stream()
                    .limit(10)
                    .toList();
            int countOfHoles = c.getHoles().size();
            int eclecticSum = 0;
            for (int i = 0; i < countOfHoles; i++) {
                eclecticSum = eclecticSum + getBestShotCountForHole(i, playedRoundsSelected);
            }

            log.info("eclecticSum {}", eclecticSum);
            return eclecticSum;
        } catch (Exception e) {
            log.info("Can't calc eclectic hcp {}", e.getMessage());
            return null;
        }
    }

    public @NonNull Map<YearMonth, Double> getEclecticByMonth(
            final String userId,
            final YearMonth start,
            final YearMonth end
    ) {
        final Map<YearMonth, Double> result = new HashMap<>();
        try {
            final List<PlayedRoundEntity> allRounds = loadAllRoundsSorted(userId);
            YearMonth cursor = start;
            while (!cursor.isAfter(end)) {
                Double value = eclecticAsOf(allRounds, cursor.atEndOfMonth());
                if (value != null) {
                    result.put(cursor, value);
                }
                cursor = cursor.plusMonths(1);
            }
        } catch (Exception e) {
            log.info("Can't calc monthly eclectic for {}: {}", userId, e.getMessage());
        }
        return result;
    }

    /**
     * Yearly chart point: last {@value #ECLECTIC_ROUND_WINDOW} rounds on the as-of home course
     * not newer than 31 Dec of that year (same rule as monthly, at year end).
     */
    public @NonNull Map<Year, Double> getEclecticByYear(
            final String userId,
            final Year start,
            final Year end
    ) {
        final Map<Year, Double> result = new HashMap<>();
        try {
            final List<PlayedRoundEntity> allRounds = loadAllRoundsSorted(userId);
            final int years = (int) ChronoUnit.YEARS.between(start, end) + 1;
            Year cursor = start;
            for (int i = 0; i < years; i++) {
                Double value = eclecticAsOf(allRounds, cursor.atMonth(12).atEndOfMonth());
                if (value != null) {
                    result.put(cursor, value);
                }
                cursor = cursor.plusYears(1);
            }
        } catch (Exception e) {
            log.info("Can't calc yearly eclectic for {}: {}", userId, e.getMessage());
        }
        return result;
    }

    private @NonNull List<PlayedRoundEntity> loadAllRoundsSorted(final String userId) {
        return playedRoundRepository.findByUserId(userId)
                .stream()
                .filter(r -> r.getDate() != null && r.getCourseName() != null)
                .sorted(Comparator
                        .comparing(PlayedRoundEntity::getDate, Comparator.reverseOrder())
                        .thenComparing(PlayedRoundEntity::getId, Comparator.reverseOrder()))
                .toList();
    }

    /**
     * Home course at {@code asOfInclusive} is the most-played course among rounds
     * on or before that date. Eclectic uses the last {@value #ECLECTIC_ROUND_WINDOW}
     * rounds on that course in the same window; fewer than 10 → null.
     */
    private @Nullable Double eclecticAsOf(
            final List<PlayedRoundEntity> allRoundsSortedDesc,
            final LocalDate asOfInclusive
    ) {
        final String courseName = homeCourseNameAsOf(allRoundsSortedDesc, asOfInclusive);
        if (courseName == null) {
            return null;
        }
        final GolfCourse course = catalog.findByName(courseName).orElse(null);
        if (course == null) {
            return null;
        }
        final List<PlayedRoundEntity> window = allRoundsSortedDesc.stream()
                .filter(r -> courseName.equals(r.getCourseName()))
                .filter(r -> !r.getDate().isAfter(asOfInclusive))
                .limit(ECLECTIC_ROUND_WINDOW)
                .toList();
        if (window.size() < ECLECTIC_ROUND_WINDOW) {
            return null;
        }
        return (double) eclecticSum(course, window);
    }

    private @Nullable String homeCourseNameAsOf(
            final List<PlayedRoundEntity> allRounds,
            final LocalDate asOfInclusive
    ) {
        final Map<String, Long> counts = allRounds.stream()
                .filter(r -> !r.getDate().isAfter(asOfInclusive))
                .map(PlayedRoundEntity::getCourseName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return counts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private int eclecticSum(final GolfCourse course, final List<PlayedRoundEntity> rounds) {
        int countOfHoles = course.getHoles().size();
        int sum = 0;
        for (int i = 0; i < countOfHoles; i++) {
            sum += getBestShotCountForHole(i, rounds);
        }
        return sum;
    }

    private int getBestShotCountForHole(int i, final List<PlayedRoundEntity> playedRoundsOfYear) {
        return playedRoundsOfYear
                .stream()
                .mapToInt(x -> x.getHoleStrokes().get(i))
                .min()
                .getAsInt();
    }

    @NonNull List<HcpScoreDTO> getEclecticHcpTimeline(final String userId){
        try {
            log.info("getEclecticHcpTimeline {}", userId);
            List<HcpScoreDTO> eclecticHcpTimeline = new ArrayList<>();

            final String courseName = getHomeCourseName(userId);
            List<PlayedRoundEntity> allPlayedRounds = playedRoundRepository.findAllByUserIdAndCourseName(userId, courseName);
            log.info("Found played rounds {}", allPlayedRounds.size());
            int[] yearsWithPlayedRounds = allPlayedRounds.stream().mapToInt(x -> x.getDate().getYear()).toArray();
            for (int yearsWithPlayedRound : yearsWithPlayedRounds) {
                eclecticHcpTimeline.add(
                        new HcpScoreDTO(
                                LocalDate.of(yearsWithPlayedRound, 1, 1),
                                getEclecticHcpForYear(yearsWithPlayedRound, userId)
                        )
                );
            }

            return eclecticHcpTimeline;
        } catch (Exception e) {
            log.info("Can't calc eclectic hcp {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public String getHomeCourseName(final String userId){
        List<PlayedRoundEntity> p = playedRoundRepository.findByUserId(userId);
        Map<String, Long> x = p
                .stream()
                .map(PlayedRoundEntity::getCourseName)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Optional<Map.Entry<String, Long>> top = x.entrySet().stream().max(Map.Entry.comparingByValue());
        log.info("top entry: {}", top);
        return top.get().getKey();
    }

    public int getCoursePar(final String courseName){
        final GolfCourse c = catalog.findByName(courseName).get();
        int coursePar = c.getHoles().stream().mapToInt(Hole::getPar).sum();
        log.info("course name {} par {}", courseName, coursePar);
        return coursePar;
    }
}
