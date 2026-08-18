package com.mirkoebert;

/**
 * Inclusive min/max for user-entered numeric fields (forms and CSV import).
 */
public final class InputLimits {

    public static final int HCP_MIN = -20;
    public static final int HCP_MAX = 56;

    public static final int SGI_POINTS_MIN = 0;
    public static final int SGI_POINTS_MAX = 40;
    public static final int SGI_TEST_ID_MIN = 1;
    public static final int SGI_TEST_ID_MAX = 8;

    public static final int METRIC_MIN = 0;
    public static final int METRIC_MAX = 99;

    public static final int HOLE_STROKES_MIN = 1;
    public static final int HOLE_STROKES_MAX = 20;

    public static final int COUNT_MIN = 0;
    public static final int COUNT_MAX = 99;

    private InputLimits() {
    }
}
