package com.mirkoebert.sgi.calc;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.function.IntUnaryOperator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for per-test points → short-game HCP lookup tables.
 */
class SgiTestHcpFunctionsTest {

    @Nested
    class Test1 {

        private final IntUnaryOperator cut = new SgiTest1HcpFunction();

        @ParameterizedTest
        @CsvSource({
                "0, 36",
                "1, 31",
                "4, 15",
                "9, 0"
        })
        void mapsKnownPointsToHcp(int points, int expectedHcp) {
            assertThat(cut.applyAsInt(points)).isEqualTo(expectedHcp);
        }

        @Test
        void returnsTourHcpForAboveTable() {
            assertThat(cut.applyAsInt(10)).isEqualTo(-1);
            assertThat(cut.applyAsInt(-1)).isEqualTo(-1);
        }
    }

    @Nested
    class Test2 {

        private final IntUnaryOperator cut = new SgiTest2HcpFunction();

        @ParameterizedTest
        @CsvSource({
                "0, 36",
                "3, 27",
                "6, 15",
                "12, 0"
        })
        void mapsKnownPointsToHcp(int points, int expectedHcp) {
            assertThat(cut.applyAsInt(points)).isEqualTo(expectedHcp);
        }

        @Test
        void returnsTourHcpForAboveTable() {
            assertThat(cut.applyAsInt(13)).isEqualTo(-1);
        }
    }

    @Nested
    class Test3 {

        private final IntUnaryOperator cut = new SgiTest3HcpFunction();

        @ParameterizedTest
        @CsvSource({
                "0, 36",
                "2, 22",
                "6, 9",
                "12, 0"
        })
        void mapsKnownPointsToHcp(int points, int expectedHcp) {
            assertThat(cut.applyAsInt(points)).isEqualTo(expectedHcp);
        }

        @Test
        void returnsTourHcpForAboveTable() {
            assertThat(cut.applyAsInt(13)).isEqualTo(-1);
        }
    }

    @Nested
    class Test4 {

        private final IntUnaryOperator cut = new SgiTest4HcpFunction();

        @ParameterizedTest
        @CsvSource({
                "0, 36",
                "2, 16",
                "5, 6",
                "9, 1",
                "10, 1",
                "11, 0"
        })
        void mapsKnownPointsToHcp(int points, int expectedHcp) {
            assertThat(cut.applyAsInt(points)).isEqualTo(expectedHcp);
        }

        @Test
        void returnsTourHcpForAboveTable() {
            assertThat(cut.applyAsInt(12)).isEqualTo(-1);
        }
    }

    @Nested
    class Test5 {

        private final IntUnaryOperator cut = new SgiTest5HcpFunction();

        @ParameterizedTest
        @CsvSource({
                "0, 52",
                "4, 35",
                "10, 17",
                "16, 0"
        })
        void mapsKnownPointsToHcp(int points, int expectedHcp) {
            assertThat(cut.applyAsInt(points)).isEqualTo(expectedHcp);
        }

        @Test
        void returnsTourHcpForAboveTable() {
            assertThat(cut.applyAsInt(17)).isEqualTo(-1);
        }
    }

    @Nested
    class Test7 {

        private final IntUnaryOperator cut = new SgiTest7HcpFunction();

        @ParameterizedTest
        @CsvSource({
                "0, 52",
                "3, 33",
                "8, 10",
                "14, 0"
        })
        void mapsKnownPointsToHcp(int points, int expectedHcp) {
            assertThat(cut.applyAsInt(points)).isEqualTo(expectedHcp);
        }

        @Test
        void returnsTourHcpForAboveTable() {
            assertThat(cut.applyAsInt(15)).isEqualTo(-1);
        }
    }

    @Nested
    class Test8 {

        private final IntUnaryOperator cut = new SgiTest8HcpFunction();

        @ParameterizedTest
        @CsvSource({
                "0, 52",
                "3, 30",
                "8, 10",
                "13, 0"
        })
        void mapsKnownPointsToHcp(int points, int expectedHcp) {
            assertThat(cut.applyAsInt(points)).isEqualTo(expectedHcp);
        }

        @Test
        void returnsTourHcpForAboveTable() {
            assertThat(cut.applyAsInt(14)).isEqualTo(-1);
        }
    }
}
