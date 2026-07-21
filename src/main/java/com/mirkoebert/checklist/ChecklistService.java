package com.mirkoebert.checklist;

import com.mirkoebert.goal.GoalEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChecklistService {

        private final GolfCheckListItemRepository golfCheckListItemRepository;
        private final GolfCheckEntityRepository golfCheckEntityRepository;

        public List<GolfCheckListItem> getForGoal(final GoalEnum goal) {
                return switch (goal) {
                        case BREAK100 -> golfCheckListItemRepository.findByGoal(GoalEnum.BREAK100.name());
                        case BREAK90 -> golfCheckListItemRepository.findByGoal(GoalEnum.BREAK90.name());
                };
        }

        public List<Long> getSelectedItemIds(final String userId, final GoalEnum goal) {
                final List<Long> itemIds = itemIdsForGoal(goal);
                if (itemIds.isEmpty()) {
                        return List.of();
                }
                return golfCheckEntityRepository.findByUserIdAndCheckListItemIdIn(userId, itemIds).stream()
                        .filter(GolfCheckEntity::isChecked)
                        .map(GolfCheckEntity::getCheckListItemId)
                        .filter(Objects::nonNull)
                        .toList();
        }

        /**
         * Replaces the user's checked items for the given goal with {@code selectedItemIds}.
         * Only IDs that belong to the goal are accepted.
         */
        @Transactional
        public void saveSelected(final String userId, final GoalEnum goal, final Collection<Long> selectedItemIds) {
                final List<Long> validItemIds = itemIdsForGoal(goal);
                if (validItemIds.isEmpty()) {
                        log.info("No checklist items for goal {}, nothing to save for user {}", goal, userId);
                        return;
                }

                final Set<Long> validIdSet = new HashSet<>(validItemIds);
                final Set<Long> selected = (selectedItemIds == null ? List.<Long>of() : selectedItemIds).stream()
                        .filter(validIdSet::contains)
                        .collect(Collectors.toCollection(HashSet::new));

                golfCheckEntityRepository.deleteByUserIdAndCheckListItemIdIn(userId, validItemIds);

                final LocalDate today = LocalDate.now();
                final List<GolfCheckEntity> toSave = new ArrayList<>(selected.size());
                for (Long itemId : selected) {
                        toSave.add(GolfCheckEntity.builder()
                                .userId(userId)
                                .checkListItemId(itemId)
                                .checked(true)
                                .date(today)
                                .build());
                }
                golfCheckEntityRepository.saveAll(toSave);
                log.info("Saved {} checklist selections for user {} goal {}", toSave.size(), userId, goal);
        }

        private List<Long> itemIdsForGoal(final GoalEnum goal) {
                return getForGoal(goal).stream()
                        .map(GolfCheckListItem::getId)
                        .filter(Objects::nonNull)
                        .toList();
        }

}
