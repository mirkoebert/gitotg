package com.mirkoebert.analytics;

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

    public List<String> analyze(GolfSnapshot snapshot) {
        Set<String> weaknesses = new LinkedHashSet<>();

        if (snapshot.handicap() != null && snapshot.shortGameIndex() != null) {
            if (snapshot.handicap() < snapshot.shortGameIndex()) {
                weaknesses.add(SHORT_GAME);
            }
            if (snapshot.shortGameIndex() < snapshot.handicap()) {
                weaknesses.add(LONG_GAME);
            }
        }

        if (snapshot.lostBalls() != null && snapshot.lostBalls() > LOST_BALLS_LIMIT) {
            weaknesses.add(LONG_GAME);
            weaknesses.add(MENTAL_GAME);
        }

        if (snapshot.handicap() != null && snapshot.handicap() > HIGH_HANDICAP) {
            weaknesses.add(COURSE_MANAGEMENT);
            weaknesses.add(MENTAL_GAME);
        }

        if (droppedBelow(snapshot.handicaps(), snapshot.today(), CLUBS_ABOVE_30)) {
            weaknesses.add(CLUBS);
        }

        if (stableForAtLeastOneYear(snapshot.handicaps(), snapshot.today())) {
            weaknesses.add(NO_PROGRESS);
        }

        if (snapshot.roundsThisYear() + snapshot.roundsLastYear() < ROUND_VOLUME) {
            weaknesses.add(PLAYING);
        }

        for (int entries : snapshot.shortGameEntriesPerTest()) {
            if (entries < ENTRIES_PER_TEST) {
                weaknesses.add(SHORT_GAME);
                break;
            }
        }

        if (droppedBelow(snapshot.handicaps(), snapshot.today(), CLUBS_ABOVE_18)) {
            weaknesses.add(CLUBS);
        }

        LocalDate lastRound = snapshot.lastPlayedRound();
        if (lastRound == null || lastRound.isBefore(snapshot.today().minusMonths(1))) {
            weaknesses.add(PLAYING);
        }

        return List.copyOf(weaknesses);
    }

    /**
     * True when a later handicap inside the last year is under {@code threshold} and the previous one was not.
     */
    private static boolean droppedBelow(List<DatedHandicap> handicaps, LocalDate today, double threshold) {
        LocalDate windowStart = today.minusYears(1);
        List<DatedHandicap> sorted = sortedUpTo(handicaps, today);
        Double previous = null;
        for (DatedHandicap point : sorted) {
            boolean insideWindow = !point.date().isBefore(windowStart);
            if (insideWindow && previous != null && previous >= threshold && point.handicap() < threshold) {
                return true;
            }
            previous = point.handicap();
        }
        return false;
    }

    /**
     * True when a handicap on or before a year ago matches every later handicap.
     */
    private static boolean stableForAtLeastOneYear(List<DatedHandicap> handicaps, LocalDate today) {
        LocalDate windowStart = today.minusYears(1);
        List<DatedHandicap> sorted = sortedUpTo(handicaps, today);
        DatedHandicap anchor = null;
        for (DatedHandicap point : sorted) {
            if (!point.date().isAfter(windowStart)) {
                anchor = point;
            }
        }
        if (anchor == null) {
            return false;
        }
        double baseline = anchor.handicap();
        for (DatedHandicap point : sorted) {
            if (!point.date().isBefore(anchor.date()) && Math.abs(point.handicap() - baseline) >= SAME_HANDICAP) {
                return false;
            }
        }
        return true;
    }

    private static List<DatedHandicap> sortedUpTo(List<DatedHandicap> handicaps, LocalDate today) {
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
