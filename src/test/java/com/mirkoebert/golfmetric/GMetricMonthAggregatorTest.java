package com.mirkoebert.golfmetric;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GMetricMonthAggregatorTest {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-yyyy");

    @Mock
    private GMetricRepository repo;

    private GMetricMonthAggregator cut;

    @BeforeEach
    void setUp() {
        cut = new GMetricMonthAggregator(repo);
        // default: no data for any type unless a test overrides it
        for (GMetricType type : GMetricType.values()) {
            when(repo.findByUserIdAndType("u", type)).thenReturn(List.of());
        }
    }

    private static GMetricEntity metric(LocalDate date, GMetricType type, int value) {
        return GMetricEntity.builder().userId("u").date(date).type(type).metricValue(value).build();
    }

    @Test
    void getMetricsForRange_nullRange_defaultsTo12MonthWindowWithNoData() {
        GMetricChartData result = cut.getMetricsForRange(null, "u");

        assertThat(result.labels()).hasSize(12);
        assertThat(result.lostBalls()).hasSize(12).containsOnlyNulls();
        assertThat(result.doubleBogey()).hasSize(12).containsOnlyNulls();
        assertThat(result.bogey()).hasSize(12).containsOnlyNulls();
        assertThat(result.labels().getLast()).isEqualTo(FMT.format(YearMonth.now()));
        assertThat(result.labels().getFirst()).isEqualTo(FMT.format(YearMonth.now().minusMonths(11)));
    }

    @Test
    void getMetricsForRange_lastYear_averagesMultipleEntriesInTheSameMonth() {
        YearMonth currentMonth = YearMonth.now();
        when(repo.findByUserIdAndType("u", GMetricType.LOST_BALLS)).thenReturn(List.of(
                metric(currentMonth.atDay(1), GMetricType.LOST_BALLS, 2),
                metric(currentMonth.atEndOfMonth(), GMetricType.LOST_BALLS, 4)
        ));

        GMetricChartData result = cut.getMetricsForRange("lastYear", "u");

        assertThat(result.lostBalls().getLast()).isEqualTo(3.0);
        assertThat(result.doubleBogey().getLast()).isNull();
        assertThat(result.bogey().getLast()).isNull();
    }

    @Test
    void getMetricsForRange_all_emptyRepo_fallsBackTo12MonthWindow() {
        when(repo.findByUserId("u")).thenReturn(List.of());

        GMetricChartData result = cut.getMetricsForRange("all", "u");

        assertThat(result.labels()).hasSize(12);
    }

    @Test
    void getMetricsForRange_all_windowStartsAtEarliestEntryMonth() {
        LocalDate earliest = YearMonth.now().minusMonths(5).atDay(15);
        GMetricEntity earliestEntry = metric(earliest, GMetricType.BOGEY, 7);
        when(repo.findByUserId("u")).thenReturn(List.of(earliestEntry));
        when(repo.findByUserIdAndType("u", GMetricType.BOGEY)).thenReturn(List.of(earliestEntry));

        GMetricChartData result = cut.getMetricsForRange("ALL", "u");

        // 5 months ago through the current month inclusive
        assertThat(result.labels()).hasSize(6);
        assertThat(result.labels().getFirst()).isEqualTo(FMT.format(YearMonth.from(earliest)));
        assertThat(result.labels().getLast()).isEqualTo(FMT.format(YearMonth.now()));
        assertThat(result.bogey().getFirst()).isEqualTo(7.0);
    }
}
