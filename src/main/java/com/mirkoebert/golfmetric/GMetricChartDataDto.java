package com.mirkoebert.golfmetric;

import java.util.List;

public record GMetricChartDataDto(
                List<String> labels,
                List<Double> lostBalls,
                List<Double> doubleBogey,
                List<Double> bogey,
                List<Double> handicap,
                List<Integer> eclectic,
                List<Double> consistency,
                List<Integer> playedRounds
) {}
