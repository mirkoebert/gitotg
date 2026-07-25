package com.mirkoebert.golfmetric;

import java.util.List;

/**
 * Chart payload for monthly gmetric series (one list per {@link GMetricType}).
 */
public record GMetricChartData(
                List<String> labels,
                List<Double> lostBalls,
                List<Double> doubleBogey,
                List<Double> bogey
) {
}
