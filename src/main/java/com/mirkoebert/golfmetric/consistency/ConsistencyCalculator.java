package com.mirkoebert.golfmetric.consistency;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;

@Service
class ConsistencyCalculator implements Function<List<Integer>, Double> {

        @Override
        public @Nullable Double apply(@Nullable final List<Integer> totals) {
                if (totals == null || totals.size() < 2) {
                        return null;
                }
                final int n = totals.size();
                final double mean = totals.stream().mapToLong(Integer::longValue).sum() / (double) n;
                final double sumOfSquaredDifferences = totals.stream().mapToDouble(total -> {
                        final double difference = total - mean;
                        return difference * difference;
                }).sum();
                final double deviation = Math.sqrt(sumOfSquaredDifferences / n);
                return BigDecimal.valueOf(deviation).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
}
