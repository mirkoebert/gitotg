package com.mirkoebert.handicap;

import com.mirkoebert.sgi.aggregator.HcpData;
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
                assertThat(r.labels()).hasSize(18);
                assertThat(r.hcp()).hasSize(18);
                assertThat(r.hcp().getFirst()).isEqualTo(32.4);
        }
}
