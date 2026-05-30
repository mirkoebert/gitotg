package com.mirkoebert.handicap;

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

        @Min(-20)
        @Max(56)
        @NotNull
        private Double hcp;
}
