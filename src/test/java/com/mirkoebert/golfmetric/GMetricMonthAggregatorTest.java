package com.mirkoebert.golfmetric;

import com.mirkoebert.golfmetric.eclectic.EclecticService;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GMetricMonthAggregatorTest {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-yyyy");
    private static final DateTimeFormatter FMT_YEAR = DateTimeFormatter.ofPattern("yyyy");
    @Mock
    private GMetricRepository repo;
    @Mock
    private HcpRepository hcpRepository;
    @Mock
    private EclecticService eclecticService;
    private GMetricMonthAggregator cut;

    @BeforeEach
    void setUp() {
        cut = new GMetricMonthAggregator(repo, hcpRepository, eclecticService);
        // default: no data for any type unless a test overrides it
        for (GMetricType type : GMetricType.values()) {
            lenient().when(repo.findByUserIdAndType("u", type)).thenReturn(List.of());
        }
        lenient().when(hcpRepository.findByUserId("u")).thenReturn(List.of());
        lenient().when(eclecticService.getEclecticByMonth(eq("u"), any(), any())).thenReturn(Map.of());
        lenient().when(eclecticService.getEclecticByYear(eq("u"), any(), any())).thenReturn(Map.of());
    }

    private static GMetricEntity metric(LocalDate date, GMetricType type, int value) {
        return GMetricEntity.builder().userId("u").date(date).type(type).metricValue(value).build();
    }

    @Test
    void getMetricsForRange_nullRange_defaultsTo12MonthWindowWithNoData() {
        GMetricChartDataDto result = cut.getMetricsForRange(null, "u");

        assertThat(result.labels()).hasSize(12);
        assertThat(result.lostBalls()).hasSize(12).containsOnlyNulls();
        assertThat(result.doubleBogey()).hasSize(12).containsOnlyNulls();
        assertThat(result.bogey()).hasSize(12).containsOnlyNulls();
        assertThat(result.handicap()).hasSize(12).containsOnlyNulls();
        assertThat(result.eclectic()).hasSize(12).containsOnlyNulls();
        assertThat(result.labels().getLast()).isEqualTo(FMT.format(YearMonth.now()));
        assertThat(result.labels().getFirst()).isEqualTo(FMT.format(YearMonth.now().minusMonths(11)));
    }

    @Test
    void getMetricsForRange_lastYear_includesMonthlyEclecticSeries() {
        YearMonth now = YearMonth.now();
        when(eclecticService.getEclecticByMonth(eq("u"), eq(now.minusMonths(11)), eq(now)))
                .thenReturn(Map.of(now, 36.0, now.minusMonths(2), 40.0));

        GMetricChartDataDto result = cut.getMetricsForRange("lastYear", "u");

        assertThat(result.eclectic()).hasSize(12);
        assertThat(result.eclectic().getLast()).isEqualTo(36.0);
        assertThat(result.eclectic().get(9)).isEqualTo(40.0);
        assertThat(result.eclectic().getFirst()).isNull();
    }

    @Test
    void getMetricsForRange_lastTwoYears_keeps24MonthsAndDropsOlderEntries() {
        YearMonth now = YearMonth.now();
        YearMonth inside = now.minusMonths(20);
        YearMonth outside = now.minusMonths(30);
        when(repo.findByUserIdAndType("u", GMetricType.BOGEY_PLUS)).thenReturn(List.of(
                metric(inside.atDay(10), GMetricType.BOGEY_PLUS, 4),
                metric(outside.atDay(10), GMetricType.BOGEY_PLUS, 9)
        ));

        GMetricChartDataDto result = cut.getMetricsForRange("lastTwoYears", "u");

        assertThat(result.labels()).hasSize(24);
        assertThat(result.labels().getFirst()).isEqualTo(FMT.format(now.minusMonths(23)));
        assertThat(result.labels().getLast()).isEqualTo(FMT.format(now));
        assertThat(result.bogey().get(3)).isEqualTo(4.0);
        assertThat(result.bogey()).doesNotContain(9.0);
        assertThat(result.lostBalls()).hasSize(24);
        assertThat(result.doubleBogey()).hasSize(24);
        assertThat(result.handicap()).hasSize(24).containsOnlyNulls();
        assertThat(result.eclectic()).hasSize(24).containsOnlyNulls();
    }

    @Test
    void getMetricsForRange_lastYear_roundsAveragesToOneDecimalAndIncludesHandicap() {
        YearMonth currentMonth = YearMonth.now();
        when(repo.findByUserIdAndType("u", GMetricType.LOST_BALLS_PER_9HOLES)).thenReturn(List.of(
                metric(currentMonth.atDay(1), GMetricType.LOST_BALLS_PER_9HOLES, 1),
                metric(currentMonth.atDay(2), GMetricType.LOST_BALLS_PER_9HOLES, 2),
                metric(currentMonth.atDay(3), GMetricType.LOST_BALLS_PER_9HOLES, 2)
        ));
        when(hcpRepository.findByUserId("u")).thenReturn(List.of(
                handicap(currentMonth.atDay(1), 2.0),
                handicap(currentMonth.atDay(20), 3.0),
                handicap(currentMonth.minusMonths(13).atDay(1), 40.0)
        ));

        GMetricChartDataDto result = cut.getMetricsForRange("lastYear", "u");

        assertThat(result.lostBalls().getLast()).isEqualTo(1.7);
        assertThat(result.handicap()).hasSize(12);
        assertThat(result.handicap().getLast()).isEqualTo(2.5);
        assertThat(result.handicap()).doesNotContain(40.0);
    }

    @Test
    void getMetricsForRange_lastTwoYears_keepsHandicapInsideThe24MonthWindow() {
        YearMonth now = YearMonth.now();
        when(hcpRepository.findByUserId("u")).thenReturn(List.of(
                handicap(now.minusMonths(13).atDay(1), 40.0),
                handicap(now.atDay(2), 2.0),
                handicap(now.atDay(12), 3.0)
        ));

        GMetricChartDataDto result = cut.getMetricsForRange("lastTwoYears", "u");

        assertThat(result.handicap()).hasSize(24);
        assertThat(result.handicap().get(10)).isEqualTo(40.0);
        assertThat(result.handicap().getLast()).isEqualTo(2.5);
    }

    @Test
    void getMetricsForRange_lastYear_averagesMultipleEntriesInTheSameMonth() {
        YearMonth currentMonth = YearMonth.now();
        when(repo.findByUserIdAndType("u", GMetricType.LOST_BALLS_PER_9HOLES)).thenReturn(List.of(
                metric(currentMonth.atDay(1), GMetricType.LOST_BALLS_PER_9HOLES, 2),
                metric(currentMonth.atEndOfMonth(), GMetricType.LOST_BALLS_PER_9HOLES, 4)
        ));

        GMetricChartDataDto result = cut.getMetricsForRange("lastYear", "u");

        assertThat(result.lostBalls().getLast()).isEqualTo(3.0);
        assertThat(result.doubleBogey().getLast()).isNull();
        assertThat(result.bogey().getLast()).isNull();
    }

    @Test
    void getMetricsForRange_all_emptyRepo_returnsEmptySeries() {
        when(repo.findByUserId("u")).thenReturn(List.of());

        GMetricChartDataDto result = cut.getMetricsForRange("all", "u");

        assertThat(result.labels()).isEmpty();
        assertThat(result.lostBalls()).isEmpty();
        assertThat(result.doubleBogey()).isEmpty();
        assertThat(result.bogey()).isEmpty();
        assertThat(result.handicap()).isEmpty();
        assertThat(result.eclectic()).isEmpty();
    }

    @Test
    void getMetricsForRange_all() {
        LocalDate earliest = YearMonth.now().minusMonths(5).atDay(15);
        GMetricEntity earliestEntry = metric(earliest, GMetricType.BOGEY_PLUS, 7);
        when(repo.findByUserId("u")).thenReturn(List.of(earliestEntry));
        when(repo.findByUserIdAndType("u", GMetricType.BOGEY_PLUS)).thenReturn(List.of(earliestEntry));

        GMetricChartDataDto result = cut.getMetricsForRange("ALL", "u");

        // 5 months ago through the current month inclusive
        assertThat(result.labels()).hasSize(1);
        assertThat(result.labels().getFirst()).isEqualTo(FMT_YEAR.format(Year.from(earliest)));
        assertThat(result.labels().getLast()).isEqualTo(FMT_YEAR.format(Year.now()));
        assertThat(result.bogey().getFirst()).isEqualTo(7.0);
        assertThat(result.handicap()).containsOnlyNulls();
    }

    @Test
    void getMetricsForRange_all_startsAtEarliestHandicapAndRoundsToOneDecimal() {
        LocalDate handicapStart = LocalDate.now().minusYears(2).withMonth(6).withDayOfMonth(1);
        LocalDate metricDate = LocalDate.now().withDayOfMonth(1);
        GMetricEntity bogey = metric(metricDate, GMetricType.BOGEY_PLUS, 3);
        when(repo.findByUserId("u")).thenReturn(List.of(bogey));
        when(repo.findByUserIdAndType("u", GMetricType.BOGEY_PLUS)).thenReturn(List.of(bogey));
        when(hcpRepository.findByUserId("u")).thenReturn(List.of(
                handicap(handicapStart, 18.24),
                handicap(metricDate, 20.0),
                handicap(metricDate.plusDays(10), 21.0)
        ));
        when(eclecticService.getEclecticByYear(eq("u"), eq(Year.from(handicapStart)), eq(Year.now())))
                .thenReturn(Map.of(Year.now(), 36.0));

        GMetricChartDataDto result = cut.getMetricsForRange("all", "u");

        assertThat(result.labels().getFirst()).isEqualTo(FMT_YEAR.format(Year.from(handicapStart)));
        assertThat(result.labels().getLast()).isEqualTo(FMT_YEAR.format(Year.now()));
        assertThat(result.handicap().getFirst()).isEqualTo(18.2);
        assertThat(result.handicap().getLast()).isEqualTo(20.5);
        assertThat(result.lostBalls().getFirst()).isNull();
        assertThat(result.bogey().getLast()).isEqualTo(3.0);
        assertThat(result.eclectic().getLast()).isEqualTo(36.0);
        assertThat(result.eclectic().getFirst()).isNull();
    }

    private static HcpScoreEntity handicap(LocalDate date, double hcp) {
        return HcpScoreEntity.builder().userId("u").date(date).hcp(hcp).build();
    }
}
