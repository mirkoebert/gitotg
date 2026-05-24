package com.mirkoebert.handicap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HcpScoreOutFormatedDTO {

        private String date;
        private String hcp;
        private String trend;
}
