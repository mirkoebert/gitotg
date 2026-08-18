package com.mirkoebert.golfmetric;

import com.mirkoebert.InputLimits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMetricDTO {

    @NotNull
    private LocalDate selectedDate;

    @Min(InputLimits.METRIC_MIN)
    @Max(InputLimits.METRIC_MAX)
    @NotNull
    private Integer metricValue;

    @NotNull
    private GMetricType type;
}
