package com.mirkoebert.golfcourse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@Import({BogeyPlusCountFunction.class, GolfCourseCatalog.class})
class BogeyPlusCountFunctionTest {

    @Autowired
    private BogeyPlusCountFunction cut;

    private static PlayedRoundDto round(final String courseName, final Integer... strokes) {
        return PlayedRoundDto.builder()
                .courseName(courseName)
                .selectedDate(LocalDate.of(2026, 1, 1))
                .holeStrokes(List.of(strokes))
                .lostBalls(0)
                .build();
    }

    @Test
    void applyAsInt_countsHolesWithStrokesExactlyTwoOverPar() {
        // Fischland pars: 5,4,2,5,3,4,4,3,4
        // hole1: 7 (par5, +2 double bogey), hole5: 5 (par3, +2 double bogey), rest match par
        int result = cut.applyAsInt(round("Fischland", 6, 4, 2, 5, 5, 4, 4, 3, 4));

        assertThat(result).isEqualTo(2);
    }

    @Test
    void applyAsInt_returnsZero_whenNoHoleIsTwoOverPar() {
        // Fischland pars: 5,4,2,5,3,4,4,3,4 - strokes match par exactly
        int result = cut.applyAsInt(round("Fischland", 5, 4, 2, 5, 3, 4, 4, 3, 4));

        assertThat(result).isZero();
    }

    @Test
    void applyAsInt_returnsZero_whenCourseUnknown() {
        int result = cut.applyAsInt(round("Unknown", 6, 4, 7));

        assertThat(result).isZero();
    }

    @Test
    void applyAsInt_returnsZero_whenHoleCountMismatches() {
        int result = cut.applyAsInt(round("Fischland", 6, 4));

        assertThat(result).isZero();
    }
}
