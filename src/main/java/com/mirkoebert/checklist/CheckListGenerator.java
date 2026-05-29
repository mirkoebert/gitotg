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
                if (repo.count() > 0) {
                        log.debug("There is data in the db. Skip loading initial data set");
                        return;
                }
                List<GolfCheckListItem> l = new ArrayList<>();
                l.add(new GolfCheckListItem(null, "Cheap Balls", "Play cheap golf balls, one buck per ball is Ok at this level. You don't benefit from expensive balls. You are loosing to much balls", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Grip", "Before you invest time in other things like your swing check and learn the right grip.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Win a tournament (netto)", "On a good day you are capable to win a club tournament.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Without driver", "Master your irons. Play without driver or 3 wood. You can break 100 without driver.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Consistent 150 yard / 145 m shot", "Carry 150 yards is all you need. Whatever it is, it's enough to break 100.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Improve your short game", "Start with short game HCP.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Short putts", "1 yard / 1m putt with consistence > 90% rate, every time", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Warm up", "Learn a good warmup routine to get juice in the joints, to loose your muscles.", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Range first", "One bucket of range balls is cheaper then loosing two balls on the course", GoalEnum.BREAK100.name()));
                l.add(new GolfCheckListItem(null, "Know your distances", "Get the carry distances for clubs from the tee.", GoalEnum.BREAK100.name()));


                l.add(new GolfCheckListItem(null, "Consistent 160 yard / 150 m shot", "Carry. Whatever it is, it's enough to break 90.", GoalEnum.BREAK90.name()));
                l.add(new GolfCheckListItem(null, "Improve your short game", "Go ahead with short game HCP.", GoalEnum.BREAK90.name()));
                l.add(new GolfCheckListItem(null, "Consistency is king", "Start with consistency games.", GoalEnum.BREAK90.name()));
                l.add(new GolfCheckListItem(null, "Preshot routine", "Preshot routine, every time, I said, every time. Start with a simple one.", GoalEnum.BREAK90.name()));
                repo.saveAll(l);
        }
}
