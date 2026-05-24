package com.mirkoebert.sgi.aggregator;

import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
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

        @Test
        void getLastOfMonth() {
                assertThat(cut).isNotNull();
                LocalDate ld1 = LocalDate.of(2025, 10, 14);
                LocalDate ld2 = LocalDate.of(2025, 10, 4);


                SingleTestResultEntity d1 = SingleTestResultEntity.builder().testId(1).hcp(1).date(ld1).build();
                SingleTestResultEntity d2 = SingleTestResultEntity.builder().testId(1).hcp(2).date(ld2).build();

                when(repo.findByUserIdAndTestId(anyString(), eq(1))).thenReturn(List.of(d2, d1));

                final HcpData r = cut.getHcpForLastMonth(6, "userId", 1);
                assertThat(r).isNotNull();
                assertThat(r.labels().get(0)).hasToString("10-2025");
                assertThat(r.hcp().get(0)).isEqualTo(1.5);
        }
}
