package com.mirkoebert.golfcourse;

import com.mirkoebert.InputLimits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayedRoundDto {

    @NotBlank
    private String courseName;

    @NotNull
    private LocalDate selectedDate;

    @NotEmpty
    private List<@NotNull @Min(InputLimits.HOLE_STROKES_MIN) @Max(InputLimits.HOLE_STROKES_MAX) Integer> holeStrokes;

    @Min(InputLimits.COUNT_MIN)
    @Max(InputLimits.COUNT_MAX)
    @NotNull
    @Builder.Default
    private Integer lostBalls = 0;

    @Min(InputLimits.COUNT_MIN)
    @Max(InputLimits.COUNT_MAX)
    @NotNull
    @Builder.Default
    private Integer doubleBogeysPlus = 0;

    @Min(InputLimits.COUNT_MIN)
    @Max(InputLimits.COUNT_MAX)
    @NotNull
    @Builder.Default
    private Integer bogeysPlus = 0;
}
