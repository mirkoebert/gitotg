package com.mirkoebert.handicap;

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
public class HcpScoreDTO {

    @NotNull
    private LocalDate selectedDate;

    @Min(InputLimits.HCP_MIN)
    @Max(InputLimits.HCP_MAX)
    @NotNull
    private Double hcp;
}
