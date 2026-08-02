package com.mirkoebert.timeline;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

class TimelineRangeTest {

    @Test
    void fromParam_parsesKnownValues() {
        assertThat(TimelineRange.fromParam("last30")).isEqualTo(TimelineRange.LAST_30);
        assertThat(TimelineRange.fromParam("last100")).isEqualTo(TimelineRange.LAST_100);
        assertThat(TimelineRange.fromParam("all")).isEqualTo(TimelineRange.ALL);
        assertThat(TimelineRange.fromParam("ALL")).isEqualTo(TimelineRange.ALL);
    }

    @Test
    void fromParam_defaultsToLast30ForUnknownOrBlank() {
        assertThat(TimelineRange.fromParam(null)).isEqualTo(TimelineRange.LAST_30);
        assertThat(TimelineRange.fromParam("")).isEqualTo(TimelineRange.LAST_30);
        assertThat(TimelineRange.fromParam("  ")).isEqualTo(TimelineRange.LAST_30);
        assertThat(TimelineRange.fromParam("nope")).isEqualTo(TimelineRange.LAST_30);
    }

    @Test
    void toPageable_usesLimitOrUnpaged() {
        assertThat(TimelineRange.LAST_30.toPageable().getPageSize()).isEqualTo(30);
        assertThat(TimelineRange.LAST_100.toPageable().getPageSize()).isEqualTo(100);
        assertThat(TimelineRange.ALL.toPageable()).isEqualTo(Pageable.unpaged());
    }
}
