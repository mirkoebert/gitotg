package com.mirkoebert.cockpit;

import com.mirkoebert.checklist.ChecklistProgress;
import com.mirkoebert.golfcourse.PlayedRoundEntity;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.handicap.HcpScoreOutFormatedDTO;

import java.time.LocalDate;

public record CockpitView(
        HcpScoreOutFormatedDTO handicap,
        Integer sgiHcp,
        MetricSnapshot lostBalls,
        MetricSnapshot bogey,
        MetricSnapshot doubleBogey,
        ChecklistProgress break100,
        ChecklistProgress break90,
        ChecklistProgress break80,
        RoundSnapshot lastRound,
        String advice
) {

    public record MetricSnapshot(Integer value, LocalDate date) {
        static MetricSnapshot from(GMetricEntity entity) {
            if (entity == null) {
                return new MetricSnapshot(null, null);
            }
            return new MetricSnapshot(entity.getMetricValue(), entity.getDate());
        }

        public boolean present() {
            return value != null;
        }
    }

    public record RoundSnapshot(String courseName, LocalDate date, int totalStrokes, int lostBalls) {
        static RoundSnapshot from(PlayedRoundEntity round) {
            if (round == null) {
                return null;
            }
            return new RoundSnapshot(
                    round.getCourseName(),
                    round.getDate(),
                    round.getTotalStrokes(),
                    round.getLostBalls()
            );
        }
    }
}
