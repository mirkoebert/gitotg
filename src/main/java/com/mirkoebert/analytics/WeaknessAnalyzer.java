package com.mirkoebert.analytics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Turns a {@link GolfSnapshot} into the weakness labels from {@code specs/analyzer.md}.
 * <p>
 * Acceptance criterion 2 says the short game index is below the short game index. That comparison is never true.
 * This class treats it as the short game index being below the handicap, which adds {@code long game}.
 */
@Component
@Slf4j
public class WeaknessAnalyzer {
        static final String SHORT_GAME = "short game";
        static final String LONG_GAME = "long game";
        static final String MENTAL_GAME = "mental game";
        static final String COURSE_MANAGEMENT = "course management";
        static final String CLUBS = "clubs";
        static final String NO_PROGRESS = "no progress";
        static final String PLAYING = "playing";
        private static final double SAME_HANDICAP = 0.05;
        private static final double HIGH_HANDICAP = 20;
        private static final double LOST_BALLS_LIMIT = 1;
        private static final int ROUND_VOLUME = 10;
        private static final int ENTRIES_PER_TEST = 5;
        private static final double CLUBS_ABOVE_30 = 30;
        private static final double CLUBS_ABOVE_18 = 18;

        public List<String> analyze(final GolfSnapshot snapshot) {
                Set<String> weaknesses = new LinkedHashSet<>();

                if (snapshot.handicap() != null && snapshot.shortGameIndex() != null) {
                        if (snapshot.handicap() < snapshot.shortGameIndex()) {
                                log.info(
                                                "Handicap {} is below short game index {}: added {}",
                                                snapshot.handicap(),
                                                snapshot.shortGameIndex(),
                                                SHORT_GAME
                                );
                                weaknesses.add(SHORT_GAME);
                        }
                        if (snapshot.shortGameIndex() < snapshot.handicap()) {
                                log.info(
                                                "Short game index {} is below handicap {}: added {}",
                                                snapshot.shortGameIndex(),
                                                snapshot.handicap(),
                                                LONG_GAME
                                );
                                weaknesses.add(LONG_GAME);
                        }
                }

                if (snapshot.lostBalls() != null && snapshot.lostBalls() > LOST_BALLS_LIMIT) {
                        log.info(
                                        "Lost balls {} are greater than {}: added {}, {}",
                                        snapshot.lostBalls(),
                                        LOST_BALLS_LIMIT,
                                        LONG_GAME,
                                        MENTAL_GAME
                        );
                        weaknesses.add(LONG_GAME);
                        weaknesses.add(MENTAL_GAME);
                }

                if (snapshot.handicap() != null && snapshot.handicap() > HIGH_HANDICAP) {
                        log.info(
                                        "Handicap {} is higher than {}: added {}, {}",
                                        snapshot.handicap(),
                                        HIGH_HANDICAP,
                                        COURSE_MANAGEMENT,
                                        MENTAL_GAME
                        );
                        weaknesses.add(COURSE_MANAGEMENT);
                        weaknesses.add(MENTAL_GAME);
                }

                String droppedBelow30 = dropReason(snapshot.handicaps(), snapshot.today(), CLUBS_ABOVE_30);
                if (droppedBelow30 != null) {
                        log.info("{}: added {}", droppedBelow30, CLUBS);
                        weaknesses.add(CLUBS);
                }

                String stable = stableReason(snapshot.handicaps(), snapshot.today());
                if (stable != null) {
                        log.info("{}: added {}", stable, NO_PROGRESS);
                        weaknesses.add(NO_PROGRESS);
                }

                long rounds = snapshot.roundsThisYear() + snapshot.roundsLastYear();
                if (rounds < ROUND_VOLUME) {
                        log.info(
                                        "Played rounds this year ({}) and last year ({}) total {}: added {}",
                                        snapshot.roundsThisYear(),
                                        snapshot.roundsLastYear(),
                                        rounds,
                                        PLAYING
                        );
                        weaknesses.add(PLAYING);
                }

                int[] entriesPerTest = snapshot.shortGameEntriesPerTest();
                for (int testIndex = 0; testIndex < entriesPerTest.length; testIndex++) {
                        int entries = entriesPerTest[testIndex];
                        if (entries < ENTRIES_PER_TEST) {
                                log.info(
                                                "Short game test {} has {} entries, fewer than {}: added {}",
                                                testIndex + 1,
                                                entries,
                                                ENTRIES_PER_TEST,
                                                SHORT_GAME
                                );
                                weaknesses.add(SHORT_GAME);
                                break;
                        }
                }

                String droppedBelow18 = dropReason(snapshot.handicaps(), snapshot.today(), CLUBS_ABOVE_18);
                if (droppedBelow18 != null) {
                        log.info("{}: added {}", droppedBelow18, CLUBS);
                        weaknesses.add(CLUBS);
                }

                LocalDate lastRound = snapshot.lastPlayedRound();
                LocalDate oneMonthAgo = snapshot.today().minusMonths(1);
                if (lastRound == null) {
                        log.info("No played round: added {}", PLAYING);
                        weaknesses.add(PLAYING);
                } else if (lastRound.isBefore(oneMonthAgo)) {
                        log.info("Last played round {} is before {}: added {}", lastRound, oneMonthAgo, PLAYING);
                        weaknesses.add(PLAYING);
                }

                return List.copyOf(weaknesses);
        }

        /**
         * Reason when a later handicap inside the last year is under {@code threshold} and the previous one was not.
         */
        private static String dropReason(
                        final List<DatedHandicap> handicaps,
                        final LocalDate today,
                        final double threshold
        ) {
                LocalDate windowStart = today.minusYears(1);
                List<DatedHandicap> sorted = sortedUpTo(handicaps, today);
                Double previous = null;
                LocalDate previousDate = null;
                for (DatedHandicap point : sorted) {
                        boolean insideWindow = !point.date().isBefore(windowStart);
                        if (insideWindow
                                        && previous != null
                                        && previous >= threshold
                                        && point.handicap() < threshold) {
                                return "Handicap dropped from "
                                                + previous
                                                + " on "
                                                + previousDate
                                                + " to "
                                                + point.handicap()
                                                + " on "
                                                + point.date()
                                                + ", below "
                                                + threshold;
                        }
                        previous = point.handicap();
                        previousDate = point.date();
                }
                return null;
        }

        /**
         * Reason when a handicap on or before a year ago matches every later handicap.
         */
        private static String stableReason(final List<DatedHandicap> handicaps, final LocalDate today) {
                LocalDate windowStart = today.minusYears(1);
                List<DatedHandicap> sorted = sortedUpTo(handicaps, today);
                DatedHandicap anchor = null;
                for (DatedHandicap point : sorted) {
                        if (!point.date().isAfter(windowStart)) {
                                anchor = point;
                        }
                }
                if (anchor == null) {
                        return null;
                }
                double baseline = anchor.handicap();
                for (DatedHandicap point : sorted) {
                        if (!point.date().isBefore(anchor.date())
                                        && Math.abs(point.handicap() - baseline) >= SAME_HANDICAP) {
                                return null;
                        }
                }
                return "Handicap " + baseline + " on " + anchor.date() + " is unchanged through " + today;
        }

        private static List<DatedHandicap> sortedUpTo(final List<DatedHandicap> handicaps, final LocalDate today) {
                List<DatedHandicap> sorted = new ArrayList<>();
                for (DatedHandicap point : handicaps) {
                        if (point.date() != null && !point.date().isAfter(today)) {
                                sorted.add(point);
                        }
                }
                sorted.sort(Comparator.comparing(DatedHandicap::date));
                return sorted;
        }
}
