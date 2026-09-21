package com.mirkoebert.golfmetric.byyear;

import com.mirkoebert.golfcourse.*;
import com.mirkoebert.handicap.HcpScoreDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
class EclecticByYearTest {

    @Autowired
    private EclecticByYear cut;
    @Autowired
    private CourseService courseService;
    @MockitoBean
    private GolfCourseCatalog catalog;
    @Autowired
    private PlayedRoundRepository playedRoundRepository;

    final String testCourseName = "test course 16";
    final String testUser = "testUser";

    @BeforeEach
    void initMock(){
        log.info("Setup Mock");

        final List<Hole> holes = List.of(
                new Hole(1,4),
                new Hole(2,4),
                new Hole(3,4),
                new Hole(4,4),
                new Hole(5,4),
                new Hole(6,4),
                new Hole(7,4),
                new Hole(8,4),
                new Hole(9,4));
        when(catalog.findByName(testCourseName)).thenReturn(Optional.of(GolfCourse.builder().name(testCourseName).holes(holes).build()));

        playedRoundRepository.deleteAll();
    }

    @Test
    void getEclecticHcpForYearHappyPath() {
        log.info("Test with one played round");
        List<Integer> played = List.of(5,4,4,4,4,4,4,4,4);
        boolean r = courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 6, 22), played, 0);
        assertTrue(r);
        assertThat(cut.getEclecticHcpForYear(2025, testCourseName, testUser)).isEqualTo(1);

        log.info("Test with two played rounds");
        played = List.of(4,4,4,4,4,4,4,4,4);
        r = courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 6, 22), played, 0);
        assertTrue(r);
        assertThat(playedRoundRepository.findByUserId(testUser)).isNotEmpty();
        assertThat(cut.getEclecticHcpForYear(2025, testCourseName, testUser)).isEqualTo(0);
    }

    @Test
    void getEclecticHcpFor_YearWithoutPlayedRound() {
        assertNull(cut.getEclecticHcpForYear(1612, testCourseName, testUser));
    }

    @Test
    void getEclecticHcpTimelineHappyPath(){
        List<Integer> played = List.of(5,4,4,4,4,4,4,4,4);
        courseService.submitRound(testUser, testCourseName, LocalDate.of(2026, 6, 22), played, 0);

        played = List.of(4,4,4,4,4,4,4,4,4);
        courseService.submitRound(testUser, testCourseName, LocalDate.of(2025, 6, 22), played, 0);

        Iterable<? extends HcpScoreDTO> expectedList = List.of(
                new HcpScoreDTO(LocalDate.of(2026, 1, 1), (double) 1),
                new HcpScoreDTO(LocalDate.of(2025, 1, 1), (double) 0)
        );

        assertThat(cut.getEclecticHcpTimeline(testCourseName, testUser)).containsExactlyElementsOf(expectedList);
    }

    @Test
    void getEclecticHcpTimelineNoResult(){
        assertThat(cut.getEclecticHcpTimeline(testCourseName, testUser)).containsExactlyElementsOf(Collections.emptyList());
    }
}
