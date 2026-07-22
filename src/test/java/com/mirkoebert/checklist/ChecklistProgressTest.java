package com.mirkoebert.checklist;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChecklistProgressTest {

    @Test
    void of_returnsZeroProgressWhenTotalIsZeroOrNegative() {
        assertThat(ChecklistProgress.of(0, 0)).isEqualTo(new ChecklistProgress(0, 0, 0));
        assertThat(ChecklistProgress.of(5, 0)).isEqualTo(new ChecklistProgress(0, 0, 0));
        assertThat(ChecklistProgress.of(3, -1)).isEqualTo(new ChecklistProgress(0, 0, 0));
    }

    @Test
    void of_clampsNegativeCheckedCountToZero() {
        ChecklistProgress progress = ChecklistProgress.of(-2, 4);

        assertThat(progress.checkedCount()).isZero();
        assertThat(progress.totalCount()).isEqualTo(4);
        assertThat(progress.percentage()).isZero();
    }

    @Test
    void of_clampsCheckedCountAboveTotalToTotal() {
        ChecklistProgress progress = ChecklistProgress.of(10, 4);

        assertThat(progress.checkedCount()).isEqualTo(4);
        assertThat(progress.totalCount()).isEqualTo(4);
        assertThat(progress.percentage()).isEqualTo(100);
    }

    @Test
    void of_computesPercentageRounded() {
        assertThat(ChecklistProgress.of(1, 2)).isEqualTo(new ChecklistProgress(1, 2, 50));
        assertThat(ChecklistProgress.of(1, 3)).isEqualTo(new ChecklistProgress(1, 3, 33));
        assertThat(ChecklistProgress.of(2, 3)).isEqualTo(new ChecklistProgress(2, 3, 67));
        assertThat(ChecklistProgress.of(0, 5)).isEqualTo(new ChecklistProgress(0, 5, 0));
        assertThat(ChecklistProgress.of(5, 5)).isEqualTo(new ChecklistProgress(5, 5, 100));
    }

    @Test
    void isEmpty_isTrueOnlyWhenTotalCountIsZero() {
        assertThat(ChecklistProgress.of(0, 0).isEmpty()).isTrue();
        assertThat(ChecklistProgress.of(2, 4).isEmpty()).isFalse();
        assertThat(new ChecklistProgress(0, 0, 0).isEmpty()).isTrue();
        assertThat(new ChecklistProgress(0, 3, 0).isEmpty()).isFalse();
    }
}
