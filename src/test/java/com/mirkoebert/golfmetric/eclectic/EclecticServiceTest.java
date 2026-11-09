package com.mirkoebert.golfmetric.eclectic;

import com.mirkoebert.playedrounds.*;
import com.mirkoebert.handicap.HcpScoreDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
class EclecticServiceTest {
        @Autowired
        private EclecticService cut;
        @Autowired
        private CourseService courseService;
        @MockitoBean
        private GolfCourseCatalog catalog;
        @Autowired
        private PlayedRoundRepository playedRoundRepository;
        final String testCourseName = "test course 16";
        final String otherTestCourseName = "test course 17";
        final String testUser = "testUser";

        @BeforeEach
        void initMock() {
                log.info("Setup Mock");

                final List<Hole> holes = List.of(
                                new Hole(1, 4),
                                new Hole(2, 4),
                                new Hole(3, 4),
                                new Hole(4, 4),
                                new Hole(5, 4),
                                new Hole(6, 4),
                                new Hole(7, 4),
                                new Hole(8, 4),
                                new Hole(9, 4)
                );
                when(catalog.findByName(testCourseName))
                        .thenReturn(Optional.of(GolfCourse.builder().name(testCourseName).holes(holes).build()));
                when(catalog.findByName(otherTestCourseName))
                        .thenReturn(Optional.of(GolfCourse.builder().name(otherTestCourseName).holes(holes).build()));

                playedRoundRepository.deleteAll();
        }

        @Test
        void getEclecticHcpForYearHappyPath() {
                log.info("Test with one played round");
                List<Integer> played = List.of(5, 4, 4, 4, 4, 4, 4, 4, 4);
                boolean r = courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 6, 22), played, 0);
                assertTrue(r);
                assertThat(cut.getEclecticHcpForYear(2025, testUser)).isEqualTo(37);

                log.info("Test with two played rounds");
                played = List.of(4, 4, 4, 4, 4, 4, 4, 4, 4);
                r = courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 6, 22), played, 0);
                assertTrue(r);
                assertThat(playedRoundRepository.findByUserId(testUser)).isNotEmpty();
                assertThat(cut.getEclecticHcpForYear(2025, testUser)).isEqualTo(36);
        }

        @Test
        void getEclecticHcpFor_YearWithoutPlayedRound() {
                assertNull(cut.getEclecticHcpForYear(1612, testUser));
        }

        @Test
        void getEclecticHcpTimelineHappyPath() {
                List<Integer> played = List.of(5, 4, 4, 4, 4, 4, 4, 4, 4);
                courseService.submitRound(testUser, testCourseName, LocalDate.of(2026, 6, 22), played, 0);

                played = List.of(4, 4, 4, 4, 4, 4, 4, 4, 4);
                courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 6, 22), played, 0);

                Iterable<? extends HcpScoreDTO> expectedList = List.of(
                                new HcpScoreDTO(LocalDate.of(2026, 1, 1), (double) 37),
                                new HcpScoreDTO(LocalDate.of(2025, 1, 1), (double) 36)
                );

                assertThat(cut.getEclecticHcpTimeline(testUser)).containsExactlyElementsOf(expectedList);
        }

        @Test
        void getEclecticHcpTimelineNoResult() {
                assertThat(cut.getEclecticHcpTimeline(testUser)).containsExactlyElementsOf(Collections.emptyList());
        }

        @Test
        void getHomeCourseName() {
                List<Integer> played = List.of(5, 5, 4, 4, 4, 4, 4, 4, 4);
                boolean r = courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 6, 22), played, 0);
                assertTrue(r);

                played = List.of(4, 4, 4, 4, 4, 4, 4, 4, 4);
                r = courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 6, 23), played, 0);
                assertTrue(r);

                played = List.of(4, 4, 4, 4, 4, 4, 4, 4, 4);
                r = courseService.submitRound(testUser, otherTestCourseName, LocalDate.of(2025, 6, 23), played, 0);
                assertTrue(r);

                assertThat(cut.getHomeCourseName(testUser)).hasToString(testCourseName);
        }

        @Test
        void getEclecticByMonth_omitsMonthsWithFewerThan10RoundsAndKeysByYearMonth() {
                List<Integer> older = List.of(9, 9, 9, 9, 9, 9, 9, 9, 9);
                List<Integer> newer = List.of(4, 4, 4, 4, 4, 4, 4, 4, 4);
                for (int i = 0; i < 9; i++) {
                        assertTrue(courseService.submitRound(
                                        testUser,
                                        testCourseName,
                                        LocalDate.of(2025, 1, 1).plusDays(i),
                                        older,
                                        0
                        ));
                }
                // 10th round completes the window in February
                assertTrue(courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 2, 5), older, 0));
                for (int i = 0; i < 10; i++) {
                        assertTrue(courseService.submitRound(
                                        testUser,
                                        testCourseName,
                                        LocalDate.of(2025, 3, 1).plusDays(i),
                                        newer,
                                        0
                        ));
                }

                Map<YearMonth, Double> byMonth =
                                cut.getEclecticByMonth(testUser, YearMonth.of(2024, 12), YearMonth.of(2025, 3));

                assertThat(byMonth).doesNotContainKey(YearMonth.of(2024, 12));
                assertThat(byMonth).doesNotContainKey(YearMonth.of(2025, 1));
                assertThat(byMonth).containsEntry(YearMonth.of(2025, 2), 81.0);
                assertThat(byMonth).containsEntry(YearMonth.of(2025, 3), 36.0);
        }

        @Test
        void getEclecticByYear_usesYearEndAsOfNotMidYearAverage() {
                List<Integer> midYear = List.of(9, 9, 9, 9, 9, 9, 9, 9, 9);
                List<Integer> lateYear = List.of(4, 4, 4, 4, 4, 4, 4, 4, 4);
                for (int i = 0; i < 10; i++) {
                        assertTrue(courseService.submitRound(
                                        testUser,
                                        testCourseName,
                                        LocalDate.of(2025, 6, 1).plusDays(i),
                                        midYear,
                                        0
                        ));
                }
                for (int i = 0; i < 10; i++) {
                        assertTrue(courseService.submitRound(
                                        testUser,
                                        testCourseName,
                                        LocalDate.of(2025, 11, 1).plusDays(i),
                                        lateYear,
                                        0
                        ));
                }

                Map<Year, Double> byYear = cut.getEclecticByYear(testUser, Year.of(2025), Year.of(2025));
                Map<YearMonth, Double> june =
                                cut.getEclecticByMonth(testUser, YearMonth.of(2025, 6), YearMonth.of(2025, 6));
                Map<YearMonth, Double> december =
                                cut.getEclecticByMonth(testUser, YearMonth.of(2025, 12), YearMonth.of(2025, 12));

                assertThat(june).containsEntry(YearMonth.of(2025, 6), 81.0);
                assertThat(december).containsEntry(YearMonth.of(2025, 12), 36.0);
                assertThat(byYear).containsEntry(Year.of(2025), 36.0);
                assertThat(byYear.get(Year.of(2025))).isEqualTo(december.get(YearMonth.of(2025, 12)));
                assertThat(byYear.get(Year.of(2025))).isNotEqualTo(june.get(YearMonth.of(2025, 6)));
        }

        @Test
        void getEclecticByMonth_usesHomeCourseAsOfEachMonth_notOverallMostPlayed() {
                List<Integer> courseAScores = List.of(9, 9, 9, 9, 9, 9, 9, 9, 9);
                List<Integer> courseBScores = List.of(4, 4, 4, 4, 4, 4, 4, 4, 4);
                // January: 10 rounds on course A only
                for (int i = 0; i < 10; i++) {
                        assertTrue(courseService.submitRound(
                                        testUser,
                                        testCourseName,
                                        LocalDate.of(2025, 1, 1).plusDays(i),
                                        courseAScores,
                                        0
                        ));
                }
                // Later: 15 rounds on course B so overall home becomes B
                for (int i = 0; i < 15; i++) {
                        assertTrue(courseService.submitRound(
                                        testUser,
                                        otherTestCourseName,
                                        LocalDate.of(2025, 4, 1).plusDays(i),
                                        courseBScores,
                                        0
                        ));
                }

                assertThat(cut.getHomeCourseName(testUser)).isEqualTo(otherTestCourseName);

                Map<YearMonth, Double> byMonth =
                                cut.getEclecticByMonth(testUser, YearMonth.of(2025, 1), YearMonth.of(2025, 4));
                // January must still use course A (then-home), not overall home B (which had 0 rounds then)
                assertThat(byMonth).containsEntry(YearMonth.of(2025, 1), 81.0);
                assertThat(byMonth).containsEntry(YearMonth.of(2025, 4), 36.0);
        }
}
