package com.mirkoebert.handicap;

import com.mirkoebert.sgi.chart.HcpData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.mirkoebert.Constants.ME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HcpMonthAggregatorTest {

        @Autowired
        private HcpMonthAggregator cut;

        @Test
        void getHcpForLastMonth() {
                HcpData r = cut.getHcpForLastMonth(600, ME);
                assertThat(r).isNotNull();
                assertThat(r.labels().size()).isGreaterThanOrEqualTo(22);
                assertThat(r.hcp().size()).isGreaterThanOrEqualTo(22);
                assertThat(r.hcp().size()).isEqualTo(r.labels().size());
                assertThat(r.hcp().getFirst()).isEqualTo(32.4);
        }
}
