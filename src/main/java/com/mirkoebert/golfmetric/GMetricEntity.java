package com.mirkoebert.golfmetric;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.CsvIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GMetricEntity {

    @Id
    @GeneratedValue
    @CsvIgnore
    private long id;
    @CsvIgnore
    private String userId;
    @CsvBindByName(column = "date")
    @CsvDate("yyyy-MM-dd")
    private LocalDate date;
    /**
     * Metric count/score; not named {@code value} (reserved in H2).
     */
    @CsvBindByName(column = "metricValue")
    private int metricValue;
    @CsvBindByName(column = "type")
    @Enumerated(EnumType.STRING)
    private GMetricType type;
}
