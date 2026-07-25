package com.mirkoebert.golfmetric;

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

        @Min(0)
        @NotNull
        private Integer metricValue;

        @NotNull
        private GMetricType type;
}
