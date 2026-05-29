package com.mirkoebert.checklist;

import com.mirkoebert.goal.GoalEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChecklistService {

        private final GolfCheckListItemRepository golfCheckListItemRepository;

        List<GolfCheckListItem> getForGoal(final GoalEnum gola) {
                return switch (gola) {
                        case BREAK100 -> golfCheckListItemRepository.findByGoal(GoalEnum.BREAK100.name());
                        case BREAK90 -> golfCheckListItemRepository.findByGoal(GoalEnum.BREAK90.name());
                };
        }

}
