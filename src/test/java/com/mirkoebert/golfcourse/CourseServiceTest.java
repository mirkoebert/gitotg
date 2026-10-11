package com.mirkoebert.golfcourse;

import com.mirkoebert.golfmetric.GMetricRepository;
import org.jspecify.annotations.NonNull;
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
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@Import({CourseService.class, DoubleBogeyPlusCountFunction.class, BogeyPlusCountFunction.class, GMetricRepository.class, GolfCourseCatalog.class})
class CourseServiceTest {

    @Autowired
    private CourseService cut;
    @MockitoBean
    private PlayedRoundRepository playedRoundRepository;
    @MockitoBean
    private GMetricRepository repo;
    @Autowired
    private GolfCourseCatalog golfCourseCatalog;


    @Test
    void submitRound_savesAndReturnsTrue_whenHoleCountMatchesCourse() {
        final List<Integer> played = List.of(5, 4, 3 , 5, 3, 4, 4, 3, 4);
        boolean result = cut.submitRound("u1", "Fischland", LocalDate.of(2026, 1, 1), played, 2);

        assertThat(result).isTrue();
        verify(playedRoundRepository).save(PlayedRoundEntity
                .builder()
                .userId("u1")
                .date(LocalDate.of(2026, 1, 1))
                .courseName("Fischland")
                .holeStrokes(played)
                .lostBalls(2)
                .doubleBogeysPlus(0)
                .bogeysPlus(0)
                .build());
        verify(repo, times(3)).save(any());
    }

    @Test
    void submitRound_returnsFalseAndDoesNotSave_whenCourseUnknown() {
        boolean result = cut.submitRound("u1", "Unknown", LocalDate.of(2026, 1, 1), List.of(5, 4), 0);

        assertThat(result).isFalse();
        verify(playedRoundRepository, never()).save(any());
        verify(repo, never()).save(any());
    }

    @Test
    void submitRound_returnsFalseAndDoesNotSave_whenHoleCountMismatches() {
        boolean result = cut.submitRound("u1", "Fischland", LocalDate.of(2026, 1, 1), List.of(5), 0);

        assertThat(result).isFalse();
        verify(playedRoundRepository, never()).save(any());
        verify(repo, never()).save(any());
    }


    @Test
    void getNormaizedValue() {
        assertThat(cut.getNormaizedValue(4, golfCourseCatalog.findByName("Fischland").get())).isEqualTo(4f);
        assertThat(cut.getNormaizedValue(4, golfCourseCatalog.findByName("Grambek").get())).isEqualTo(2f);
        assertThat(cut.getNormaizedValue(5, golfCourseCatalog.findByName("Grambek").get())).isEqualTo(2.5f);
        assertThat(cut.getNormaizedValue(null, golfCourseCatalog.findByName("Grambek").get())).isNull();
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
