package com.mirkoebert.sgi.chart;

import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import({MonthlySgiHcpAggregator.class})
class MonthlySgiHcpAggregatorTest {

        @Autowired
        private MonthlySgiHcpAggregator cut;
        @MockitoBean
        private SingleTestResultRepository repo;
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-yyyy");

        @Test
        void getHcpLastOfMonth() {
                assertThat(cut).isNotNull();
                val ld1 = LocalDate.now().minusMonths(2);
                val ld2 = ld1.minusDays(1);


                val d1 = SingleTestResultEntity.builder().testId(1).hcp(1).date(ld1).build();
                val d2 = SingleTestResultEntity.builder().testId(1).hcp(2).date(ld2).build();

                when(repo.findByUserIdAndTestId(anyString(), eq(1))).thenReturn(List.of(d2, d1));

                final HcpData r = cut.getHcpForLastMonth(6, "userId", 1);

                assertThat(r).isNotNull();
                assertThat(r.labels().size()).isLessThanOrEqualTo(6);
                assertThat(r.labels().getFirst()).hasToString(ld1.format(fmt));
                assertThat(r.hcp().getFirst()).isEqualTo(1.5);
        }

        @Test
        void testWithWrongCount(){
                final HcpData r = cut.getHcpForLastMonth(-6, "userId", 1);
                assertThat(r).isNotNull();
        }


        @Test
        void testWithEmptyList(){
                when(repo.findByUserIdAndTestId(anyString(), eq(1))).thenReturn(Collections.emptyList());

                final HcpData r = cut.getHcpForLastMonth(6, "userId", 1);

                assertThat(r).isNotNull();
        }
}
