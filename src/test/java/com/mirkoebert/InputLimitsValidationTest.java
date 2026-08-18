package com.mirkoebert;

import com.mirkoebert.golfcourse.RoundDto;
import com.mirkoebert.golfmetric.GMetricDTO;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpScoreDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InputLimitsValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void gmetricValueMustBeWithinMetricLimits() {
        GMetricDTO over = metric(InputLimits.METRIC_MAX + 1);
        GMetricDTO under = metric(InputLimits.METRIC_MIN - 1);
        GMetricDTO maxOk = metric(InputLimits.METRIC_MAX);
        GMetricDTO minOk = metric(InputLimits.METRIC_MIN);

        assertThat(validator.validate(over)).isNotEmpty();
        assertThat(validator.validate(under)).isNotEmpty();
        assertThat(validator.validate(maxOk)).isEmpty();
        assertThat(validator.validate(minOk)).isEmpty();
    }

    @Test
    void hcpMustBeWithinHandicapLimits() {
        HcpScoreDTO over = hcp(InputLimits.HCP_MAX + 1.0);
        HcpScoreDTO under = hcp(InputLimits.HCP_MIN - 1.0);
        HcpScoreDTO maxOk = hcp((double) InputLimits.HCP_MAX);
        HcpScoreDTO minOk = hcp((double) InputLimits.HCP_MIN);

        assertThat(validator.validate(over)).isNotEmpty();
        assertThat(validator.validate(under)).isNotEmpty();
        assertThat(validator.validate(maxOk)).isEmpty();
        assertThat(validator.validate(minOk)).isEmpty();
    }

    @Test
    void holeStrokesAndLostBallsMustBeWithinLimits() {
        RoundDto overStrokes = round(List.of(InputLimits.HOLE_STROKES_MAX + 1), 0);
        RoundDto overLost = round(List.of(InputLimits.HOLE_STROKES_MIN), InputLimits.COUNT_MAX + 1);
        RoundDto ok = round(List.of(InputLimits.HOLE_STROKES_MAX), InputLimits.COUNT_MAX);

        assertThat(validator.validate(overStrokes)).isNotEmpty();
        assertThat(validator.validate(overLost)).isNotEmpty();
        assertThat(validator.validate(ok)).isEmpty();
    }

    private static GMetricDTO metric(int value) {
        return GMetricDTO.builder()
                .selectedDate(LocalDate.of(2026, 1, 1))
                .metricValue(value)
                .type(GMetricType.LOST_BALLS)
                .build();
    }

    private static HcpScoreDTO hcp(double value) {
        return HcpScoreDTO.builder()
                .selectedDate(LocalDate.of(2026, 1, 1))
                .hcp(value)
                .build();
    }

    private static RoundDto round(List<Integer> strokes, int lostBalls) {
        return RoundDto.builder()
                .courseName("Fischland")
                .selectedDate(LocalDate.of(2026, 1, 1))
                .holeStrokes(strokes)
                .lostBalls(lostBalls)
                .build();
    }
}
