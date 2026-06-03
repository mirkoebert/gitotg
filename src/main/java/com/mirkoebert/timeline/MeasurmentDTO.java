package com.mirkoebert.timeline;

import com.mirkoebert.GolfType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;

@Value
@Builder
public class MeasurmentDTO {

        Long id;
        String userId;
        String value;
        LocalDate date;
        GolfType type;
        String comment;

}
