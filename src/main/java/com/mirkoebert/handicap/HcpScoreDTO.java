package com.mirkoebert.handicap;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HcpScoreDTO {

        @NotNull
        @NotBlank
        @NotEmpty
        private String selectedDate;

        @Min(-20)
        @Max(56)
        @NotNull
        private Double hcp;
}
