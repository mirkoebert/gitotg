package com.mirkoebert.golfmetric;

import org.jspecify.annotations.Nullable;

/**
 * Chart window for golf-metric series. Wire values are the query parameter on
 * {@code GET /api/gmetric/chart-data}.
 */
public enum GMetricRange {
        LAST_YEAR("lastYear", 12),
        LAST_TWO_YEARS("lastTwoYears", 24),
        ALL("all", null);
        /**
         * Compile-time default for {@code @RequestParam}. Must stay equal to {@link #LAST_YEAR}'s param.
         */
        public static final String DEFAULT_PARAM = "lastYear";
        private final String param;
        @Nullable
        private final Integer months;

        GMetricRange(String param, @Nullable Integer months) {
                this.param = param;
                this.months = months;
        }

        public String param() {
                return param;
        }

        /**
         * Fixed month window for the monthly charts. {@code null} for {@link #ALL}, which is yearly.
         */
        public @Nullable Integer months() {
                return months;
        }

        /**
         * Parses a request parameter. Unknown, blank, and {@code null} values use {@link #LAST_YEAR}.
         * Comparison is case-insensitive and does not trim.
         */
        public static GMetricRange fromParam(@Nullable String param) {
                if (param == null) {
                        return LAST_YEAR;
                }
                for (GMetricRange range : values()) {
                        if (range.param.equalsIgnoreCase(param)) {
                                return range;
                        }
                }
                return LAST_YEAR;
        }
}
