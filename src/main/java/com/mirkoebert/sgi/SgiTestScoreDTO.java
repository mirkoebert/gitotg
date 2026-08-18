package com.mirkoebert.sgi;

import com.mirkoebert.InputLimits;
import com.mirkoebert.TestSuite;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SgiTestScoreDTO {


    @Min(InputLimits.SGI_POINTS_MIN)
    @Max(InputLimits.SGI_POINTS_MAX)
    @NotNull
    private Integer points;

    @NotNull
    private TestSuite type;

    @Min(InputLimits.SGI_TEST_ID_MIN)
    @Max(InputLimits.SGI_TEST_ID_MAX)
    @NotNull
    private Integer testId;
}
