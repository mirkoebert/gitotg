package com.mirkoebert.timeline;

import com.mirkoebert.GolfType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class MeasurmentDTO {

        String userId;
        String value;
        LocalDate date;
        GolfType type;
        String comment;

}
