package com.mirkoebert.analytics;

import java.time.LocalDate;
import java.util.List;

/**
 * Facts the weakness rules read. {@code shortGameEntriesPerTest} is one count per short-game test, in test id order.
 */
public record GolfSnapshot(Double handicap, Integer shortGameIndex, Double lostBalls, List<DatedHandicap> handicaps, long roundsThisYear, long roundsLastYear, int[] shortGameEntriesPerTest, LocalDate lastPlayedRound, LocalDate today) {

    public static final int SHORT_GAME_TEST_COUNT = 8;

    public GolfSnapshot {
        handicaps = handicaps == null
                ? List.of()
                : List.copyOf(handicaps);
        if (shortGameEntriesPerTest == null || shortGameEntriesPerTest.length != SHORT_GAME_TEST_COUNT) {
            throw new IllegalArgumentException("expected " + SHORT_GAME_TEST_COUNT + " short game test counts");
        }
        shortGameEntriesPerTest = shortGameEntriesPerTest.clone();
        if (today == null) {
            throw new IllegalArgumentException("today is required");
        }
    }
}
