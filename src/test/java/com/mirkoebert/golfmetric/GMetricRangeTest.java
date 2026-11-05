package com.mirkoebert.golfmetric;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GMetricRangeTest {

    @Test
    void fromParam_parsesKnownValuesIgnoringCase() {
        assertThat(GMetricRange.fromParam("lastYear")).isEqualTo(GMetricRange.LAST_YEAR);
        assertThat(GMetricRange.fromParam("LASTYEAR")).isEqualTo(GMetricRange.LAST_YEAR);
        assertThat(GMetricRange.fromParam("lastTwoYears")).isEqualTo(GMetricRange.LAST_TWO_YEARS);
        assertThat(GMetricRange.fromParam("all")).isEqualTo(GMetricRange.ALL);
        assertThat(GMetricRange.fromParam("ALL")).isEqualTo(GMetricRange.ALL);
    }

    @Test
    void fromParam_defaultsToLastYearForUnknownOrBlank() {
        assertThat(GMetricRange.fromParam(null)).isEqualTo(GMetricRange.LAST_YEAR);
        assertThat(GMetricRange.fromParam("")).isEqualTo(GMetricRange.LAST_YEAR);
        assertThat(GMetricRange.fromParam("  ")).isEqualTo(GMetricRange.LAST_YEAR);
        assertThat(GMetricRange.fromParam("nope")).isEqualTo(GMetricRange.LAST_YEAR);
    }

    @Test
    void months_matchesTheChartWindows() {
        assertThat(GMetricRange.LAST_YEAR.months()).isEqualTo(12);
        assertThat(GMetricRange.LAST_TWO_YEARS.months()).isEqualTo(24);
        assertThat(GMetricRange.ALL.months()).isNull();
    }

    @Test
    void defaultParam_matchesLastYearWireValue() {
        assertThat(GMetricRange.DEFAULT_PARAM).isEqualTo(GMetricRange.LAST_YEAR.param());
    }
}
