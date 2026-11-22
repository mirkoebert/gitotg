package com.mirkoebert.golfmetric.consistency;

import com.mirkoebert.playedrounds.CourseService;
import com.mirkoebert.playedrounds.PlayedRoundRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ConsistencyTimelineTest {

        private static final String USER_ID = "consistency-timeline-user";
        private static final String COURSE = "Fischland";
        @Autowired
        private ConsistencyService cut;
        @Autowired
        private CourseService courseService;
        @Autowired
        private PlayedRoundRepository playedRoundRepository;

        @BeforeEach
        void deleteFixtureRounds() {
                playedRoundRepository.findByUserId(USER_ID).forEach(playedRoundRepository::delete);
        }

        @Test
        void getConsistencyByMonth_omitsMonth_whenFewerThan10Rounds() {
                for (int day = 1; day <= 9; day++) {
                        submit(LocalDate.of(2025, 1, day), 40);
                }

                Map<YearMonth, Double> byMonth =
                                cut.getConsistencyByMonth(USER_ID, YearMonth.of(2025, 1), YearMonth.of(2025, 1));

                assertThat(byMonth).doesNotContainKey(YearMonth.of(2025, 1));
                assertThat(byMonth).doesNotContainValue(null);
        }

        @Test
        void getConsistencyByMonth_returnsPopulationDeviation_forLast10Rounds() {
                submitJanuaryWindow();

                Map<YearMonth, Double> byMonth =
                                cut.getConsistencyByMonth(USER_ID, YearMonth.of(2024, 12), YearMonth.of(2025, 1));

                assertThat(byMonth).doesNotContainKey(YearMonth.of(2024, 12));
                assertThat(byMonth).containsEntry(YearMonth.of(2025, 1), 2.9);
        }

        @Test
        void getConsistencyByMonth_ignoresRoundsAfterMonthEnd() {
                submitJanuaryWindow();
                for (int day = 1; day <= 10; day++) {
                        submit(LocalDate.of(2025, 3, day), 50);
                }

                Map<YearMonth, Double> byMonth =
                                cut.getConsistencyByMonth(USER_ID, YearMonth.of(2025, 1), YearMonth.of(2025, 3));

                assertThat(byMonth).containsEntry(YearMonth.of(2025, 1), 2.9);
                assertThat(byMonth).containsEntry(YearMonth.of(2025, 2), 2.9);
                assertThat(byMonth).containsEntry(YearMonth.of(2025, 3), 0.0);
        }

        @Test
        void getConsistencyByYear_usesDecember31_YearWindow() {
                for (int day = 1; day <= 10; day++) {
                        submit(LocalDate.of(2025, 1, day), 39 + day);
                }
                for (int day = 1; day <= 10; day++) {
                        submit(LocalDate.of(2025, 12, day), 60);
                }

                Map<Year, Double> byYear = cut.getConsistencyByYear(USER_ID, Year.of(2025), Year.of(2025));

                assertThat(byYear).containsEntry(Year.of(2025), 8.0);
        }


        @Test
        void getConsistencyByYear_usesDecember31_ExtendedWindow() {
                for (int day = 1; day <= 10; day++) {
                        submit(LocalDate.of(2024, 1, day), 39 + day);
                }
                for (int day = 1; day <= 10; day++) {
                        submit(LocalDate.of(2025, 12, day), 60);
                }

                Map<Year, Double> byYear = cut.getConsistencyByYear(USER_ID, Year.of(2025), Year.of(2025));

                assertThat(byYear).containsEntry(Year.of(2025), 8.0);
        }

        private void submitJanuaryWindow() {
                for (int day = 1; day <= 10; day++) {
                        submit(LocalDate.of(2025, 1, day), 39 + day);
                }
        }

        private void submit(LocalDate date, int total) {
                assertThat(courseService.submitRound(USER_ID, COURSE, date, strokesForTotal(total), 0)).isTrue();
        }

        /**
         * Fischland pars are 5,4,3,5,3,4,4,3,4 (sum 35). Extra strokes sit on hole 9.
         */
        private static List<Integer> strokesForTotal(int total) {
                int hole9 = 4 + (total - 35);
                return List.of(5, 4, 3, 5, 3, 4, 4, 3, hole9);
        }
}
