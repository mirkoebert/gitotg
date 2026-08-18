package com.mirkoebert.cockpit;

import com.mirkoebert.advisor.AdvisorService;
import com.mirkoebert.checklist.ChecklistService;
import com.mirkoebert.goal.GoalEnum;
import com.mirkoebert.golfcourse.CourseService;
import com.mirkoebert.golfmetric.GMetricService;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpService;
import com.mirkoebert.sgi.SgiHcpAggregatedService;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CockpitService {

    private final HcpService hcpService;
    private final SgiHcpAggregatedService sgiHcpAggregatedService;
    private final SingleTestResultRepository singleTestResultRepository;
    private final GMetricService gMetricService;
    private final ChecklistService checklistService;
    private final CourseService courseService;
    private final AdvisorService advisorService;

    public @NonNull CockpitView load(@NonNull String userId) {
        Integer sgiHcp = singleTestResultRepository.countByUserId(userId) > 0
                ? sgiHcpAggregatedService.getLatestSgiHcpAggregated(userId)
                : null;

        var lastRound = courseService.findRoundsForUser(userId).stream()
                .findFirst()
                .map(CockpitView.RoundSnapshot::from)
                .orElse(null);

        return new CockpitView(
                hcpService.findLatestByUserId(userId),
                sgiHcp,
                CockpitView.MetricSnapshot.from(
                        gMetricService.findLatestByUserIdAndType(userId, GMetricType.LOST_BALLS).orElse(null)),
                CockpitView.MetricSnapshot.from(
                        gMetricService.findLatestByUserIdAndType(userId, GMetricType.BOGEY).orElse(null)),
                CockpitView.MetricSnapshot.from(
                        gMetricService.findLatestByUserIdAndType(userId, GMetricType.DOUBLE_BOGEY).orElse(null)),
                checklistService.getProgress(userId, GoalEnum.BREAK100),
                checklistService.getProgress(userId, GoalEnum.BREAK90),
                checklistService.getProgress(userId, GoalEnum.BREAK80),
                lastRound,
                advisorService.getAdvise(userId)
        );
    }
}
