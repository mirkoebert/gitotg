package com.mirkoebert.handicap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

class HandicapClassifierTest {

    private HandicapClassifier cut = new HandicapClassifier();

    @Test
    void apply_returnsScratchPlayerForHandicapLessThan1() {
        assertThat(cut.apply(0.9)).isEqualTo(HandicapClassifier.SCRATCH_PLAYER);
        assertThat(cut.apply(0.0)).isEqualTo(HandicapClassifier.SCRATCH_PLAYER);
        assertThat(cut.apply(-5.0)).isEqualTo(HandicapClassifier.SCRATCH_PLAYER);
    }

    @Test
    void apply_returnsSingleFigurePlayerForHandicapFrom1ToUnder10() {
        assertThat(cut.apply(1.0)).isEqualTo(HandicapClassifier.SINGLE_FIGURE_PLAYER);
        assertThat(cut.apply(5.4)).isEqualTo(HandicapClassifier.SINGLE_FIGURE_PLAYER);
        assertThat(cut.apply(9.9)).isEqualTo(HandicapClassifier.SINGLE_FIGURE_PLAYER);
    }

    @Test
    void apply_returnsLowHandicaperFor10To15() {
        assertThat(cut.apply(10.0)).isEqualTo(HandicapClassifier.LOW_HANDICAPER);
        assertThat(cut.apply(12.3)).isEqualTo(HandicapClassifier.LOW_HANDICAPER);
        assertThat(cut.apply(15.0)).isEqualTo(HandicapClassifier.LOW_HANDICAPER);
    }

    @Test
    void apply_returnsHandicaperForOver15To25() {
        assertThat(cut.apply(15.1)).isEqualTo(HandicapClassifier.MID_HANDICAPER);
        assertThat(cut.apply(20.0)).isEqualTo(HandicapClassifier.MID_HANDICAPER);
        assertThat(cut.apply(25.0)).isEqualTo(HandicapClassifier.MID_HANDICAPER);
    }

    @Test
    void apply_returnsHighHandicaperForOver25() {
        assertThat(cut.apply(25.1)).isEqualTo(HandicapClassifier.HIGH_HANDICAPER);
        assertThat(cut.apply(30.0)).isEqualTo(HandicapClassifier.HIGH_HANDICAPER);
        assertThat(cut.apply(99.9)).isEqualTo(HandicapClassifier.HIGH_HANDICAPER);
    }

    @Test
    void apply_returnsLowHandicaperForNull() {
        assertThat(cut.apply(null)).isEqualTo(HandicapClassifier.UNKNOWN);
    }
}
