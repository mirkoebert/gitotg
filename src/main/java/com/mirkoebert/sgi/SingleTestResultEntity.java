package com.mirkoebert.sgi;

import com.mirkoebert.TestSuite;
import com.opencsv.bean.CsvIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
public class SingleTestResultEntity {

        @Id
        @GeneratedValue
        @CsvIgnore
        private long id;
        private LocalDate date;
        private Integer points;
        private TestSuite testType;
        private Integer testId;
        @CsvIgnore
        private Integer hcp;
        @CsvIgnore
        private String userId;
}
