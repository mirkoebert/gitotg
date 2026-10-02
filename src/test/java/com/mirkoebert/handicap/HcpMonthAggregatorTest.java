package com.mirkoebert.handicap;

import com.mirkoebert.sgi.chart.HcpData;
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
class HcpMonthAggregatorTest {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MM-yyyy");

    @Mock
    private HcpRepository repo;

    private HcpMonthAggregator cut;

    @BeforeEach
    void setUp() {
        cut = new HcpMonthAggregator(repo);
    }

    private static HcpScoreEntity score(LocalDate date, double hcp) {
        return HcpScoreEntity.builder().userId("u").date(date).hcp(hcp).build();
    }

    @Test
    void getHcpForLastMonth_trimsLeadingEmptyMonthsAndAveragesTheFirstMonth() {
        YearMonth oldest = YearMonth.now().minusMonths(5);
        when(repo.findByUserId("u")).thenReturn(List.of(
                score(oldest.atDay(1), 32.4),
                score(oldest.atEndOfMonth(), 30.0)
        ));

        HcpData r = cut.getHcpForLastMonth(12, "u");

        assertThat(r.labels().getFirst()).isEqualTo(FMT.format(oldest));
        assertThat(r.hcp().getFirst()).isEqualTo(31.2);
        assertThat(r.hcp()).hasSameSizeAs(r.labels());
        assertThat(r.labels().getLast()).isEqualTo(FMT.format(YearMonth.now()));
    }

    @Test
    void getHcpForLastMonth_emptyUser_keepsTheFullWindowOfNulls() {
        when(repo.findByUserId("UNKNOWN")).thenReturn(List.of());

        HcpData r = cut.getHcpForLastMonth(12, "UNKNOWN");

        assertThat(r.labels()).hasSize(12);
        assertThat(r.hcp()).hasSize(12).containsOnlyNulls();
        assertThat(r.labels().getLast()).isEqualTo(FMT.format(YearMonth.now()));
        assertThat(r.labels().getFirst()).isEqualTo(FMT.format(YearMonth.now().minusMonths(11)));
    }

    @Test
    void getHcpForRange_lastTwoYears_dropsMonthsOlderThan24AndTrimsLeadingGaps() {
        YearMonth olderThanWindow = YearMonth.now().minusMonths(30);
        YearMonth insideWindow = YearMonth.now().minusMonths(2);
        when(repo.findByUserId("u")).thenReturn(List.of(
                score(olderThanWindow.atDay(1), 40.0),
                score(insideWindow.atDay(1), 20.0)
        ));

        HcpData r = cut.getHcpForRange("lastTwoYears", "u");

        assertThat(r.labels()).doesNotContain(FMT.format(olderThanWindow));
        assertThat(r.labels().getFirst()).isEqualTo(FMT.format(insideWindow));
        assertThat(r.hcp().getFirst()).isEqualTo(20.0);
        assertThat(r.labels()).hasSize(3);
        assertThat(r.labels().getLast()).isEqualTo(FMT.format(YearMonth.now()));
    }

    @Test
    void getHcpForRange_all_startsAtTheEarliestMonth() {
        YearMonth earliest = YearMonth.now().minusMonths(30);
        when(repo.findByUserId("u")).thenReturn(List.of(
                score(earliest.atDay(1), 40.0),
                score(YearMonth.now().atDay(1), 10.0)
        ));

        HcpData r = cut.getHcpForRange("ALL", "u");

        assertThat(r.labels()).hasSize(31);
        assertThat(r.labels().getFirst()).isEqualTo(FMT.format(earliest));
        assertThat(r.hcp().getFirst()).isEqualTo(40.0);
        assertThat(r.hcp().getLast()).isEqualTo(10.0);
        assertThat(r.labels().getLast()).isEqualTo(FMT.format(YearMonth.now()));
    }

    @Test
    void getHcpForRange_unknownOrEmptyAll_usesA24MonthWindow() {
        when(repo.findByUserId("u")).thenReturn(List.of());

        HcpData unknown = cut.getHcpForRange(null, "u");
        HcpData all = cut.getHcpForRange("all", "u");

        assertThat(unknown.labels()).hasSize(24);
        assertThat(unknown.hcp()).containsOnlyNulls();
        assertThat(unknown.labels().getFirst()).isEqualTo(FMT.format(YearMonth.now().minusMonths(23)));
        assertThat(all.labels()).containsExactlyElementsOf(unknown.labels());
    }
}
