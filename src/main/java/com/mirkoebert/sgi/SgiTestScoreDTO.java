package com.mirkoebert.sgi;

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


        @Min(0)
        @Max(40)
        @NotNull
        private Integer points;

        @NotNull
        private TestSuite type;

        @Min(1)
        @Max(10)
        @NotNull
        private Integer testId;
}
