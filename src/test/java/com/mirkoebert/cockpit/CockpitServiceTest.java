package com.mirkoebert.cockpit;

import com.mirkoebert.advisor.AdvisorService;
import com.mirkoebert.checklist.ChecklistProgress;
import com.mirkoebert.checklist.ChecklistService;
import com.mirkoebert.goal.GoalEnum;
import com.mirkoebert.golfcourse.CourseService;
import com.mirkoebert.golfcourse.PlayedRoundEntity;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricService;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpScoreOutFormatedDTO;
import com.mirkoebert.handicap.HcpService;
import com.mirkoebert.sgi.SgiHcpAggregatedService;
import com.mirkoebert.sgi.SingleTestResultRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Import(CockpitService.class)
class CockpitServiceTest {

    private static final String USER = "user-123";

    @Autowired
    private CockpitService cut;

    @MockitoBean
    private HcpService hcpService;
    @MockitoBean
    private SgiHcpAggregatedService sgiHcpAggregatedService;
    @MockitoBean
    private SingleTestResultRepository singleTestResultRepository;
    @MockitoBean
    private GMetricService gMetricService;
    @MockitoBean
    private ChecklistService checklistService;
    @MockitoBean
    private CourseService courseService;
    @MockitoBean
    private AdvisorService advisorService;

    @Test
    void load_emptyUser_hasNoSgiHcpRoundOrMetrics() {
        stubEmptyHcpAndGoals();
        when(singleTestResultRepository.countByUserId(USER)).thenReturn(0);
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.LOST_BALLS)).thenReturn(Optional.empty());
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.BOGEY_PLUS)).thenReturn(Optional.empty());
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.DOUBLE_BOGEY_PLUS)).thenReturn(Optional.empty());
        when(courseService.findRoundsForUser(USER)).thenReturn(List.of());
        when(advisorService.getAdvise(USER)).thenReturn("Start a test.");

        CockpitView view = cut.load(USER);

        assertThat(view.sgiHcp()).isNull();
        assertThat(view.lostBalls().present()).isFalse();
        assertThat(view.bogey().present()).isFalse();
        assertThat(view.doubleBogey().present()).isFalse();
        assertThat(view.lastRound()).isNull();
        assertThat(view.break100().percentage()).isZero();
        assertThat(view.advice()).isEqualTo("Start a test.");
    }

    @Test
    void load_populatedUser_mapsLatestValuesAndLastRound() {
        when(hcpService.findLatestByUserId(USER)).thenReturn(HcpScoreOutFormatedDTO.builder()
                .hcp("18.4").date("01. January 2026").trend("improving").build());
        when(singleTestResultRepository.countByUserId(USER)).thenReturn(3);
        when(sgiHcpAggregatedService.getLatestSgiHcpAggregated(USER)).thenReturn(31);
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.LOST_BALLS))
                .thenReturn(Optional.of(metric(GMetricType.LOST_BALLS, 2)));
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.BOGEY_PLUS))
                .thenReturn(Optional.of(metric(GMetricType.BOGEY_PLUS, 5)));
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.DOUBLE_BOGEY_PLUS))
                .thenReturn(Optional.of(metric(GMetricType.DOUBLE_BOGEY_PLUS, 1)));
        when(checklistService.getProgress(USER, GoalEnum.BREAK100)).thenReturn(ChecklistProgress.of(4, 8));
        when(checklistService.getProgress(USER, GoalEnum.BREAK90)).thenReturn(ChecklistProgress.of(1, 4));
        when(checklistService.getProgress(USER, GoalEnum.BREAK80)).thenReturn(ChecklistProgress.of(0, 2));
        when(courseService.findRoundsForUser(USER)).thenReturn(List.of(
                PlayedRoundEntity.builder()
                        .courseName("Fischland")
                        .date(LocalDate.of(2026, 2, 1))
                        .holeStrokes(List.of(5, 4, 2))
                        .lostBalls(1)
                        .build(),
                PlayedRoundEntity.builder()
                        .courseName("Tessin")
                        .date(LocalDate.of(2026, 1, 1))
                        .holeStrokes(List.of(4, 5, 3))
                        .lostBalls(0)
                        .build()
        ));
        when(advisorService.getAdvise(USER)).thenReturn("Work the short game.");

        CockpitView view = cut.load(USER);

        assertThat(view.handicap().getHcp()).isEqualTo("18.4");
        assertThat(view.handicap().getTrend()).isEqualTo("improving");
        assertThat(view.sgiHcp()).isEqualTo(31);
        assertThat(view.lostBalls().value()).isEqualTo(2);
        assertThat(view.bogey().value()).isEqualTo(5);
        assertThat(view.doubleBogey().value()).isEqualTo(1);
        assertThat(view.break100().percentage()).isEqualTo(50);
        assertThat(view.lastRound().courseName()).isEqualTo("Fischland");
        assertThat(view.lastRound().totalStrokes()).isEqualTo(11);
        assertThat(view.lastRound().lostBalls()).isEqualTo(1);
        assertThat(view.advice()).isEqualTo("Work the short game.");
    }

    @Test
    void load_hcpWithoutSgiOrRounds_leavesThoseCardsEmpty() {
        when(hcpService.findLatestByUserId(USER)).thenReturn(HcpScoreOutFormatedDTO.builder()
                .hcp("22.0").date("02. January 2026").trend("stable").build());
        when(singleTestResultRepository.countByUserId(USER)).thenReturn(0);
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.LOST_BALLS)).thenReturn(Optional.empty());
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.BOGEY_PLUS)).thenReturn(Optional.empty());
        when(gMetricService.findLatestByUserIdAndType(USER, GMetricType.DOUBLE_BOGEY_PLUS)).thenReturn(Optional.empty());
        stubEmptyGoals();
        when(courseService.findRoundsForUser(USER)).thenReturn(List.of());
        when(advisorService.getAdvise(USER)).thenReturn("Keep going.");

        CockpitView view = cut.load(USER);

        assertThat(view.handicap().getHcp()).isEqualTo("22.0");
        assertThat(view.sgiHcp()).isNull();
        assertThat(view.lastRound()).isNull();
        assertThat(view.lostBalls().present()).isFalse();
    }

    private void stubEmptyHcpAndGoals() {
        when(hcpService.findLatestByUserId(USER)).thenReturn(HcpScoreOutFormatedDTO.builder()
                .hcp("not enough data available")
                .date("not enough data available")
                .trend("not enough data available")
                .build());
        stubEmptyGoals();
    }

    private void stubEmptyGoals() {
        when(checklistService.getProgress(USER, GoalEnum.BREAK100)).thenReturn(ChecklistProgress.of(0, 8));
        when(checklistService.getProgress(USER, GoalEnum.BREAK90)).thenReturn(ChecklistProgress.of(0, 4));
        when(checklistService.getProgress(USER, GoalEnum.BREAK80)).thenReturn(ChecklistProgress.of(0, 2));
    }

    private static GMetricEntity metric(GMetricType type, int value) {
        return GMetricEntity.builder()
                .userId(USER)
                .date(LocalDate.of(2026, 3, 1))
                .type(type)
                .metricValue(value)
                .build();
    }
}
