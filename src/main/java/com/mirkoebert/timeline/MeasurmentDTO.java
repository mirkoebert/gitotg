package com.mirkoebert.timeline;

import com.mirkoebert.GolfType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class MeasurmentDTO {

        private String userId;
        private String value;
        private LocalDate date;
        private GolfType type;
        private String comment;

}
