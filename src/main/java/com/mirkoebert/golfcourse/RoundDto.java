package com.mirkoebert.golfcourse;

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
public class RoundDto {

    @NotBlank
    private String courseName;

    @NotNull
    private LocalDate selectedDate;

    @NotEmpty
    private List<@NotNull @Min(1) Integer> holeStrokes;

    @Min(0)
    @NotNull
    @Builder.Default
    private Integer lostBalls = 0;

    @Min(0)
    @NotNull
    @Builder.Default
    private Integer doubleBogeys = 0;

    @Min(0)
    @NotNull
    @Builder.Default
    private Integer bogeys = 0;
}
