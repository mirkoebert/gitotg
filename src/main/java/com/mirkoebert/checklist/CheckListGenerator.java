package com.mirkoebert.checklist;

import com.mirkoebert.goal.GoalEnum;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CheckListGenerator {
        
        private final GolfCheckListItemRepository repo;
        
        @PostConstruct
        void init() {
                List<GolfCheckListItem> l = new ArrayList<>();
                l.add(new GolfCheckListItem(1L, "Cheap Balls", "Play cheap golf balls, one buck per ball is Ok at this level. You don't benefit from expensive balls. You are losing too many balls", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(2L, "Improve Grip", "Before you invest time in other things like your swing, check and learn the right grip.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(3L, "Win a tournament (netto)", "On a good day you are capable to win a club tournament.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(4L, "Play without driver", "Master your irons. Play without driver or 3 wood. You can break 100 without driver.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(5L, "Consistent 150 yard / 145 m shot", "Carry 150 yards is all you need. Whatever it is, it's enough to break 100.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(6L, "Improve your short game", "Short game improvements are the fastes way to lower your handicapStart with short game HCP.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(7L, "Short putts", "1 yard / 1m putt with consistency > 90% rate, every time", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(8L, "Warm up before playing", "Learn a good warmup routine to get juice in the joints, to loosen your muscles.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(9L, "Range first", "One bucket of range balls is cheaper than losing two balls on the course", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(10L, "Know your distances", "Get the carry distances for clubs from the tee.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(11L, "Learn to calc the distances", "Learn to estimate the distances for your shot.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(12L, "Play many rounds under 100", "If you break 100 then go to break 90.", GoalEnum.BREAK100.name()));

                l.add(new GolfCheckListItem(13L, "Consistent 160 yard / 150 m shot", "Carry. Whatever it is, it's enough to break 90.", GoalEnum.BREAK90.name()));
                l.add(new GolfCheckListItem(14L, "Improve your short game", "Go ahead with short game HCP.", GoalEnum.BREAK90.name()));
                l.add(new GolfCheckListItem(15L, "Consistency is king", "Start with consistency games on the range and on the greens.", GoalEnum.BREAK90.name()));
                l.add(new GolfCheckListItem(16L, "Preshot routine", "Preshot routine, every time, I said, every time. Start with a simple one.", GoalEnum.BREAK90.name()));
                l.add(new GolfCheckListItem(17L, "Collect game statistics", "Identify your weaknesses", GoalEnum.BREAK90.name()));
                l.add(new GolfCheckListItem(18L, "Play many rounds under 90", "If you break 90 then go to break 80.", GoalEnum.BREAK90.name()));


                l.add(new GolfCheckListItem(19L, "Play many rounds under 80", "If you break 80, you are ready to win the club championship.", GoalEnum.BREAK80.name()));


                if (repo.count() == l.size()) {
                        log.debug("There is data in the db. Skip loading initial data set");
                        return;
                }
                repo.deleteAll();
                repo.saveAll(l);
        }
}
