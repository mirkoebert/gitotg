package com.mirkoebert.cockpit;

import com.mirkoebert.advisor.AdvisorService;
import com.mirkoebert.checklist.ChecklistService;
import com.mirkoebert.goal.GoalEnum;
import com.mirkoebert.golfcourse.CourseService;
import com.mirkoebert.golfmetric.GMetricService;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.golfmetric.eclectic.EclecticService;
import com.mirkoebert.handicap.HcpService;
import com.mirkoebert.sgi.SgiHcpAggregatedService;
import com.mirkoebert.sgi.SingleTestResultRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

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
    private final EclecticService eclecticService;

    public @NonNull CockpitView load(@NonNull final  String userId) {
        Integer sgiHcp = singleTestResultRepository.countByUserId(userId) > 0
                ? sgiHcpAggregatedService.getLatestSgiHcpAggregated(userId)
                : null;

        final CockpitView.RoundSnapshot lastRound = courseService.findRoundsForUser(userId).stream()
                .findFirst()
                .map(CockpitView.RoundSnapshot::from)
                .orElse(null);

        final String homeCourse = homeCourseName(userId);
        return new CockpitView(
                hcpService.findLatestByUserId(userId),
                sgiHcp,
                CockpitView.MetricSnapshot.from(gMetricService.findLatestByUserIdAndType(userId, GMetricType.LOST_BALLS_PER_9HOLES)),
                CockpitView.MetricSnapshot.from(gMetricService.findLatestByUserIdAndType(userId, GMetricType.BOGEY_PLUS)),
                CockpitView.MetricSnapshot.from(gMetricService.findLatestByUserIdAndType(userId, GMetricType.DOUBLE_BOGEY_PLUS)),
                checklistService.getProgress(userId, GoalEnum.BREAK100),
                checklistService.getProgress(userId, GoalEnum.BREAK90),
                checklistService.getProgress(userId, GoalEnum.BREAK80),
                checklistService.getProgress(userId, GoalEnum.ALLTIME),
                lastRound,
                advisorService.getAdvise(userId),
                eclecticService.getEclecticHcpForLast10Rounds(userId),
                homeCourse,
                coursePar(userId, homeCourse)
        );
    }

    private String homeCourseName(String userId) {
        try {
            return eclecticService.getHomeCourseName(userId);
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    private Integer coursePar(String userId, String homeCourse) {
        if (homeCourse == null) {
            return null;
        }
        try {
            return eclecticService.getCoursePar(userId, homeCourse);
        } catch (NoSuchElementException ex) {
            return null;
        }
    }
}
