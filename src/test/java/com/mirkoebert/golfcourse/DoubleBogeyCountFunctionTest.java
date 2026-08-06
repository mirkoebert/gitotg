package com.mirkoebert.golfcourse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoubleBogeyCountFunctionTest {

    @Mock
    private GolfCourseCatalog catalog;

    @InjectMocks
    private DoubleBogeyCountFunction cut;

    private static GolfCourse course(final int... pars) {
        return GolfCourse.builder()
                .name("Fischland")
                .holes(List.of(
                        Hole.builder().number(1).par(pars[0]).build(),
                        Hole.builder().number(2).par(pars[1]).build(),
                        Hole.builder().number(3).par(pars[2]).build()))
                .build();
    }

    private static RoundDto round(final Integer... strokes) {
        return RoundDto.builder()
                .courseName("Fischland")
                .selectedDate(LocalDate.of(2026, 1, 1))
                .holeStrokes(List.of(strokes))
                .lostBalls(0)
                .build();
    }

    @Test
    void applyAsInt_countsHolesWithStrokesExactlyTwoOverPar() {
        when(catalog.findByName("Fischland")).thenReturn(Optional.of(course(4, 3, 5)));

        // hole1: 6 (par4, +2 double bogey), hole2: 4 (par3, +1 bogey), hole3: 7 (par5, +2 double bogey)
        int result = cut.applyAsInt(round(6, 4, 7));

        assertThat(result).isEqualTo(2);
    }

    @Test
    void applyAsInt_returnsZero_whenNoHoleIsTwoOverPar() {
        when(catalog.findByName("Fischland")).thenReturn(Optional.of(course(4, 3, 5)));

        int result = cut.applyAsInt(round(4, 3, 5));

        assertThat(result).isZero();
    }

    @Test
    void applyAsInt_returnsZero_whenCourseUnknown() {
        when(catalog.findByName("Unknown")).thenReturn(Optional.empty());

        int result = cut.applyAsInt(RoundDto.builder()
                .courseName("Unknown")
                .selectedDate(LocalDate.of(2026, 1, 1))
                .holeStrokes(List.of(6, 4, 7))
                .lostBalls(0)
                .build());

        assertThat(result).isZero();
    }

    @Test
    void applyAsInt_returnsZero_whenHoleCountMismatches() {
        when(catalog.findByName("Fischland")).thenReturn(Optional.of(course(4, 3, 5)));

        int result = cut.applyAsInt(round(6, 4));

        assertThat(result).isZero();
    }
}
