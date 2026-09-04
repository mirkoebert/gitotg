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
}
