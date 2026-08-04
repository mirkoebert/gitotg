package com.mirkoebert.sgi.calc;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Covers the testId -> lookup-table dispatch itself; the individual tables
 * are covered by {@link SgiTestHcpFunctionsTest}.
 */
class PointsToSgiHcpFunctionTest {

    private final PointsToSgiHcpFunction cut = new PointsToSgiHcpFunction(
            new SgiTest1HcpFunction(),
            new SgiTest2HcpFunction(),
            new SgiTest3HcpFunction(),
            new SgiTest4HcpFunction(),
            new SgiTest5HcpFunction(),
            new SgiTest7HcpFunction(),
            new SgiTest8HcpFunction()
    );

    @Test
    void apply_dispatchesToMatchingTestFunction() {
        assertThat(cut.apply(1, 0)).isEqualTo(new SgiTest1HcpFunction().applyAsInt(0));
        assertThat(cut.apply(2, 3)).isEqualTo(new SgiTest2HcpFunction().applyAsInt(3));
        assertThat(cut.apply(3, 2)).isEqualTo(new SgiTest3HcpFunction().applyAsInt(2));
        assertThat(cut.apply(4, 2)).isEqualTo(new SgiTest4HcpFunction().applyAsInt(2));
        assertThat(cut.apply(7, 3)).isEqualTo(new SgiTest7HcpFunction().applyAsInt(3));
        assertThat(cut.apply(8, 3)).isEqualTo(new SgiTest8HcpFunction().applyAsInt(3));
    }

    @Test
    void apply_test5And6BothUseTheTest5Table() {
        // testId 6 has no dedicated lookup class; it deliberately shares test 5's table.
        int expected = new SgiTest5HcpFunction().applyAsInt(4);
        assertThat(cut.apply(5, 4)).isEqualTo(expected);
        assertThat(cut.apply(6, 4)).isEqualTo(expected);
    }

    @Test
    void apply_returnsFallbackHcpForUnknownTestId() {
        assertThat(cut.apply(0, 5)).isEqualTo(99);
        assertThat(cut.apply(9, 5)).isEqualTo(99);
    }
}
