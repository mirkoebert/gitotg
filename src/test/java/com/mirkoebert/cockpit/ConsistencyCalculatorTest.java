package com.mirkoebert.cockpit;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class ConsistencyCalculatorTest {
        @Test
        void ofStrokeTotals_returnsNullWhenTotalsAreNull() {
                assertThat(ConsistencyCalculator.ofStrokeTotals(null)).isNull();
        }

        @Test
        void ofStrokeTotals_returnsNullWhenOnlyOneTotal() {
                assertThat(ConsistencyCalculator.ofStrokeTotals(List.of(40))).isNull();
        }

        @Test
        void ofStrokeTotals_returnsPopulationStandardDeviationRoundedHalfUp() {
                // Population standard deviation of 40..49 is ~2.872, half-up to one decimal.
                assertThat(ConsistencyCalculator.ofStrokeTotals(List.of(40, 41, 42, 43, 44, 45, 46, 47, 48, 49)))
                        .isEqualTo(2.9);
        }
}
