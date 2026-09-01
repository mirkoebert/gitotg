package com.mirkoebert.checklist;

import com.mirkoebert.goal.GoalEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import({ChecklistService.class})
class ChecklistServiceTest {

    @Autowired
    private ChecklistService cut;

    @MockitoBean
    private ChecklistCatalog checklistCatalog;

    @MockitoBean
    private GolfCheckEntityRepository golfCheckEntityRepository;

    private static ChecklistItem item(long id, String slug) {
        return new ChecklistItem(id, "checklist." + slug + "." + id + ".name", null);
    }

    @Test
    void getSelectedItemIds_returnsOnlyCheckedItemsForGoal() {
        when(checklistCatalog.items(GoalEnum.BREAK100))
                .thenReturn(List.of(
                        item(1L, "break100"),
                        item(2L, "break100"),
                        item(3L, "break100")
                ));
        when(golfCheckEntityRepository.findByUserIdAndCheckListItemIdIn(eq("user-1"), any()))
                .thenReturn(List.of(
                        GolfCheckEntity.builder().userId("user-1").checkListItemId(1L).checked(true).build(),
                        GolfCheckEntity.builder().userId("user-1").checkListItemId(2L).checked(false).build()
                ));

        List<Long> selected = cut.getSelectedItemIds("user-1", GoalEnum.BREAK100);

        assertThat(selected).containsExactly(1L);
    }

    @Test
    void saveSelected_replacesChecksAndIgnoresIdsOutsideGoal() {
        when(checklistCatalog.items(GoalEnum.BREAK90))
                .thenReturn(List.of(
                        item(10L, "break90"),
                        item(11L, "break90")
                ));

        cut.saveSelected("user-1", GoalEnum.BREAK90, List.of(10L, 999L));

        verify(golfCheckEntityRepository).deleteByUserIdAndCheckListItemIdIn(
                eq("user-1"), eq(List.of(10L, 11L)));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<GolfCheckEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(golfCheckEntityRepository).saveAll(captor.capture());

        List<GolfCheckEntity> saved = captor.getValue();
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst().getCheckListItemId()).isEqualTo(10L);
        assertThat(saved.getFirst().getUserId()).isEqualTo("user-1");
        assertThat(saved.getFirst().isChecked()).isTrue();
        assertThat(saved.getFirst().getDate()).isNotNull();
    }

    @Test
    void saveSelected_withEmptySelection_clearsAllChecksForGoal() {
        when(checklistCatalog.items(GoalEnum.BREAK100))
                .thenReturn(List.of(item(1L, "break100"), item(2L, "break100")));

        cut.saveSelected("user-1", GoalEnum.BREAK100, List.of());

        verify(golfCheckEntityRepository).deleteByUserIdAndCheckListItemIdIn(
                eq("user-1"), eq(List.of(1L, 2L)));
        verify(golfCheckEntityRepository).saveAll(List.of());
    }

    @Test
    void getProgress_returnsPercentageOfCheckedItems() {
        when(checklistCatalog.items(GoalEnum.BREAK100))
                .thenReturn(List.of(
                        item(1L, "break100"),
                        item(2L, "break100"),
                        item(3L, "break100"),
                        item(4L, "break100")
                ));
        when(golfCheckEntityRepository.findByUserIdAndCheckListItemIdIn(eq("user-1"), any()))
                .thenReturn(List.of(
                        GolfCheckEntity.builder().userId("user-1").checkListItemId(1L).checked(true).build(),
                        GolfCheckEntity.builder().userId("user-1").checkListItemId(3L).checked(true).build()
                ));

        ChecklistProgress progress = cut.getProgress("user-1", GoalEnum.BREAK100);

        assertThat(progress.checkedCount()).isEqualTo(2);
        assertThat(progress.totalCount()).isEqualTo(4);
        assertThat(progress.percentage()).isEqualTo(50);
    }

    @Test
    void getProgress_returnsZeroWhenChecklistIsEmpty() {
        when(checklistCatalog.items(GoalEnum.BREAK90))
                .thenReturn(List.of());

        ChecklistProgress progress = cut.getProgress("user-1", GoalEnum.BREAK90);

        assertThat(progress.checkedCount()).isZero();
        assertThat(progress.totalCount()).isZero();
        assertThat(progress.percentage()).isZero();
        assertThat(progress.isEmpty()).isTrue();
    }
}
