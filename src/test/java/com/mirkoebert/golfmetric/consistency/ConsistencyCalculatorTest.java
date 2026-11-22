package com.mirkoebert.golfmetric.consistency;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ConsistencyCalculatorTest {
        private final ConsistencyCalculator cut = new ConsistencyCalculator();

        @Test
        void apply_returnsNullWhenTotalsAreNull() {
                assertThat(cut.apply(null)).isNull();
        }

        @Test
        void apply_returnsNullWhenOnlyOneTotal() {
                assertThat(cut.apply(List.of(40))).isNull();
        }

        @Test
        void apply_returnsPopulationStandardDeviationRoundedHalfUp() {
                // Population standard deviation of 40..49 is ~2.872, half-up to one decimal.
                assertThat(cut.apply(List.of(40, 41, 42, 43, 44, 45, 46, 47, 48, 49))).isEqualTo(2.9);
        }
}
