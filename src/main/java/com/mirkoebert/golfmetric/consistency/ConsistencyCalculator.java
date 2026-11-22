package com.mirkoebert.golfmetric.consistency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class ConsistencyCalculator {
        private ConsistencyCalculator() {
        }

        public static Double ofStrokeTotals(List<Integer> totals) {
                if (totals == null || totals.size() < 2) {
                        return null;
                }
                final int n = totals.size();
                long sum = 0L;
                for (Integer total : totals) {
                        sum += total;
                }
                final double mean = sum / (double) n;
                double sumOfSquaredDifferences = 0d;
                for (Integer total : totals) {
                        final double difference = total - mean;
                        sumOfSquaredDifferences += difference * difference;
                }
                final double deviation = Math.sqrt(sumOfSquaredDifferences / n);
                return BigDecimal.valueOf(deviation).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
}
