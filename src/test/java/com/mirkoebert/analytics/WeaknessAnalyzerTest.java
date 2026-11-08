package com.mirkoebert.analytics;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WeaknessAnalyzerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 15);

    private final WeaknessAnalyzer cut = new WeaknessAnalyzer();

    @Test
    void analyze_returnsEmpty_whenEveryRuleIsQuiet() {
        assertThat(cut.analyze(baseline().build())).isEmpty();
    }

    @Test
    void analyze_addsShortGame_whenHandicapIsBelowTheShortGameIndex() {
        GolfSnapshot snapshot = baseline().handicap(14.0).shortGameIndex(15).build();

        assertThat(cut.analyze(snapshot)).containsExactly("short game");
    }

    @Test
    void analyze_addsLongGame_whenShortGameIndexIsBelowTheHandicap() {
        GolfSnapshot snapshot = baseline().handicap(16.0).shortGameIndex(15).build();

        assertThat(cut.analyze(snapshot)).containsExactly("long game");
    }

    @Test
    void analyze_addsNeitherGame_whenHandicapEqualsTheShortGameIndex() {
        assertThat(cut.analyze(baseline().build())).doesNotContain("short game", "long game");
    }

    @Test
    void analyze_skipsTheIndexComparison_whenEitherIndexIsMissing() {
        assertThat(cut.analyze(baseline().handicap(null).shortGameIndex(10).build())).isEmpty();
        assertThat(cut.analyze(baseline().shortGameIndex(null).build())).isEmpty();
    }

    @Test
    void analyze_addsLongGameAndMentalGame_whenLostBallsAreGreaterThanOne() {
        assertThat(cut.analyze(baseline().lostBalls(1.1).build())).containsExactly("long game", "mental game");
    }

    @Test
    void analyze_skipsLostBalls_whenTheValueIsOneOrMissing() {
        assertThat(cut.analyze(baseline().lostBalls(1.0).build())).isEmpty();
        assertThat(cut.analyze(baseline().lostBalls(null).build())).isEmpty();
    }

    @Test
    void analyze_addsCourseManagementAndMentalGame_whenHandicapIsHigherThan20() {
        GolfSnapshot snapshot = baseline().handicap(21.0).shortGameIndex(21).build();

        assertThat(cut.analyze(snapshot)).containsExactly("course management", "mental game");
    }

    @Test
    void analyze_skipsCourseManagement_whenHandicapIs20() {
        GolfSnapshot snapshot = baseline().handicap(20.0).shortGameIndex(20).build();

        assertThat(cut.analyze(snapshot)).isEmpty();
    }

    @Test
    void analyze_addsClubs_whenHandicapDropsBelow30InTheLastYear() {
        GolfSnapshot snapshot = baseline().handicap(20.0).shortGameIndex(20).handicaps(new DatedHandicap(TODAY.minusMonths(8), 32.0), new DatedHandicap(TODAY.minusMonths(2), 20.0)).build();

        assertThat(cut.analyze(snapshot)).containsExactly("clubs");
    }

    @Test
    void analyze_addsClubs_whenHandicapDropsBelow18InTheLastYear() {
        GolfSnapshot snapshot = baseline().handicap(17.0).shortGameIndex(17).handicaps(new DatedHandicap(TODAY.minusMonths(6), 19.0), new DatedHandicap(TODAY.minusMonths(1), 17.0)).build();

        assertThat(cut.analyze(snapshot)).containsExactly("clubs");
    }

    @Test
    void analyze_addsClubsOnce_whenTheSameDropCrosses30And18() {
        GolfSnapshot snapshot = baseline().handicap(17.0).shortGameIndex(17).handicaps(new DatedHandicap(TODAY.minusYears(2), 36.0), new DatedHandicap(TODAY.minusMonths(3), 17.0)).build();

        assertThat(cut.analyze(snapshot)).containsExactly("clubs");
    }

    @Test
    void analyze_skipsClubs_whenTheDropIsOlderThanOneYear() {
        GolfSnapshot snapshot = baseline().handicap(10.0).shortGameIndex(10).handicaps(new DatedHandicap(TODAY.minusYears(2), 40.0), new DatedHandicap(TODAY.minusYears(1).minusDays(1), 10.0), new DatedHandicap(TODAY, 10.0)).build();

        assertThat(cut.analyze(snapshot)).containsExactly("no progress");
    }

    @Test
    void analyze_addsNoProgress_whenHandicapIsUnchangedForAtLeastOneYear() {
        GolfSnapshot snapshot = baseline().handicaps(new DatedHandicap(TODAY.minusYears(1), 15.0), new DatedHandicap(TODAY, 15.0)).build();

        assertThat(cut.analyze(snapshot)).containsExactly("no progress");
    }

    @Test
    void analyze_skipsNoProgress_whenTheHandicapMovedDuringTheYear() {
        GolfSnapshot snapshot = baseline().handicap(14.0).shortGameIndex(14).handicaps(new DatedHandicap(TODAY.minusYears(1), 15.0), new DatedHandicap(TODAY, 14.0)).build();

        assertThat(cut.analyze(snapshot)).isEmpty();
    }

    @Test
    void analyze_addsPlaying_whenThisYearAndLastYearTogetherHaveFewerThanTenRounds() {
        assertThat(cut.analyze(baseline().rounds(6, 3).build())).containsExactly("playing");
    }

    @Test
    void analyze_skipsPlayingVolume_whenTheTwoYearsTogetherReachTenRounds() {
        assertThat(cut.analyze(baseline().rounds(6, 4).build())).isEmpty();
    }

    @Test
    void analyze_addsShortGame_whenAnyTestHasFewerThanFiveEntries() {
        int[] entries = fiveEach();
        entries[7] = 4;

        assertThat(cut.analyze(baseline().entries(entries).build())).containsExactly("short game");
    }

    @Test
    void analyze_addsPlaying_whenTheLastRoundIsMoreThanOneMonthAgo() {
        assertThat(cut.analyze(baseline().lastRound(TODAY.minusMonths(1).minusDays(1)).build())).containsExactly("playing");
    }

    @Test
    void analyze_addsPlaying_whenThePlayerHasNoRound() {
        assertThat(cut.analyze(baseline().lastRound(null).build())).containsExactly("playing");
    }

    @Test
    void analyze_skipsRecency_whenTheLastRoundIsExactlyOneMonthAgo() {
        assertThat(cut.analyze(baseline().lastRound(TODAY.minusMonths(1)).build())).isEmpty();
    }

    private static SnapshotBuilder baseline() {
        return new SnapshotBuilder();
    }

    private static int[] fiveEach() {
        return new int[]{5, 5, 5, 5, 5, 5, 5, 5};
    }

    private static final class SnapshotBuilder {
        private Double handicap = 15.0;
        private Integer shortGameIndex = 15;
        private Double lostBalls = 1.0;
        private List<DatedHandicap> handicaps = List.of(new DatedHandicap(TODAY, 15.0));
        private long roundsThisYear = 10;
        private long roundsLastYear = 0;
        private int[] entries = fiveEach();
        private LocalDate lastRound = TODAY;

        SnapshotBuilder handicap(final Double value) {
            this.handicap = value;
            return this;
        }

        SnapshotBuilder shortGameIndex(final Integer value) {
            this.shortGameIndex = value;
            return this;
        }

        SnapshotBuilder lostBalls(final Double value) {
            this.lostBalls = value;
            return this;
        }

        SnapshotBuilder handicaps(final DatedHandicap... values) {
            this.handicaps = List.of(values);
            return this;
        }

        SnapshotBuilder rounds(final long thisYear, final long lastYear) {
            this.roundsThisYear = thisYear;
            this.roundsLastYear = lastYear;
            return this;
        }

        SnapshotBuilder entries(final int[] values) {
            this.entries = values;
            return this;
        }

        SnapshotBuilder lastRound(final LocalDate value) {
            this.lastRound = value;
            return this;
        }

        GolfSnapshot build() {
            return new GolfSnapshot(handicap, shortGameIndex, lostBalls, new ArrayList<>(handicaps), roundsThisYear, roundsLastYear, entries, lastRound, TODAY);
        }
    }
}
