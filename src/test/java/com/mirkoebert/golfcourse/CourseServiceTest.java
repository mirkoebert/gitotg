package com.mirkoebert.golfcourse;

import com.mirkoebert.golfmetric.GMetricRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import({CourseService.class, DoubleBogeyPlusCountFunction.class, BogeyPlusCountFunction.class, GMetricRepository.class})
class CourseServiceTest {

    @Autowired
    private CourseService cut;
    @MockitoBean
    private GolfCourseCatalog catalog;
    @MockitoBean
    private PlayedRoundRepository playedRoundRepository;
    @MockitoBean
    private GMetricRepository repo;

    private static GolfCourse course() {
        return GolfCourse
                .builder()
                .name("Fischland")
                .holes(List.of(
                        Hole.builder().number(1).par(5).build(),
                        Hole.builder().number(2).par(4).build()))
                .build();
    }

    @Test
    void findAllCourses_delegatesToCatalog() {
        List<GolfCourse> courses = List.of(course());
        when(catalog.findAll()).thenReturn(courses);

        assertThat(cut.findAllCourses()).isEqualTo(courses);
    }

    @Test
    void findRoundsForUser_delegatesToRepositoryOrderedByDateDesc() {
        PlayedRoundEntity round = PlayedRoundEntity.builder().userId("u1").date(LocalDate.of(2026, 1, 1))
                .courseName("Fischland").holeStrokes(List.of(5, 4)).build();
        when(playedRoundRepository.findTop10ByUserIdOrderByDateDesc("u1")).thenReturn(List.of(round));

        assertThat(cut.findRoundsForUser("u1")).containsExactly(round);
    }

    @Test
    void submitRound_savesAndReturnsTrue_whenHoleCountMatchesCourse() {
        when(catalog.findByName("Fischland")).thenReturn(Optional.of(course()));

        // hole1: 7 strokes on a par 5 (+2, double bogey), hole2: 4 strokes on a par 4 (0)
        boolean result = cut.submitRound("u1", "Fischland", LocalDate.of(2026, 1, 1), List.of(7, 4), 2);

        assertThat(result).isTrue();
        verify(playedRoundRepository).save(PlayedRoundEntity
                .builder()
                .userId("u1")
                .date(LocalDate.of(2026, 1, 1))
                .courseName("Fischland")
                .holeStrokes(List.of(7, 4))
                .lostBalls(2)
                .doubleBogeysPlus(1)
                .bogeysPlus(1)
                .build());
    }

    @Test
    void submitRound_returnsFalseAndDoesNotSave_whenCourseUnknown() {
        when(catalog.findByName("Unknown")).thenReturn(Optional.empty());

        boolean result = cut.submitRound("u1", "Unknown", LocalDate.of(2026, 1, 1), List.of(5, 4), 0);

        assertThat(result).isFalse();
        verify(playedRoundRepository, never()).save(any());
    }

    @Test
    void submitRound_returnsFalseAndDoesNotSave_whenHoleCountMismatches() {
        when(catalog.findByName("Fischland")).thenReturn(Optional.of(course()));

        boolean result = cut.submitRound("u1", "Fischland", LocalDate.of(2026, 1, 1), List.of(5), 0);

        assertThat(result).isFalse();
        verify(playedRoundRepository, never()).save(any());
    }

    @Test
    void deleteRound_deletes_whenRoundBelongsToUser() {
        PlayedRoundEntity round = PlayedRoundEntity.builder().id(42L).userId("u1").build();
        when(playedRoundRepository.findById(42L)).thenReturn(Optional.of(round));

        cut.deleteRound("u1", 42L);

        verify(playedRoundRepository).deleteById(42L);
    }

    @Test
    void deleteRound_doesNotDelete_whenRoundBelongsToAnotherUser() {
        PlayedRoundEntity round = PlayedRoundEntity.builder().id(42L).userId("someoneElse").build();
        when(playedRoundRepository.findById(42L)).thenReturn(Optional.of(round));

        cut.deleteRound("u1", 42L);

        verify(playedRoundRepository, never()).deleteById(any());
    }

    @Test
    void deleteRound_doesNotDelete_whenRoundNotFound() {
        when(playedRoundRepository.findById(42L)).thenReturn(Optional.empty());

        cut.deleteRound("u1", 42L);

        verify(playedRoundRepository, never()).deleteById(any());
    }
}
