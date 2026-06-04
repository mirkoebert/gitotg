package com.mirkoebert.handicap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class HandicapClassifier implements Function<Double, String> {

    public static final String HIGH_HANDICAPER = "High Handicaper";
    public static final String MID_HANDICAPER = "Mid Handicaper";
    public static final String LOW_HANDICAPER = "Low Handicaper";
    public static final String SCRATCH_PLAYER = "Scratch Player";
    public static final String SINGLE_FIGURE_PLAYER = "Single Figure Player";
    public static final String UNKNOWN = "No Handicap given.";

    @Override
    public @NonNull String apply(@Nullable final Double hcp) {
        if (hcp == null) {
            return UNKNOWN;
        }
        final double v = hcp;
        if (v < 1) {
            return SCRATCH_PLAYER;
        } else if (v < 10) {
            return SINGLE_FIGURE_PLAYER;
        } else if (v > 25) {
            return HIGH_HANDICAPER;
        } else if (v > 15) {
            return MID_HANDICAPER;
        } else {
            return LOW_HANDICAPER;
        }
    }
}
