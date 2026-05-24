package com.mirkoebert.handicap;

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
public class HcpScoreEntity {

        @Id
        @GeneratedValue
        @CsvIgnore
        private long id;
        @CsvIgnore
        private String userId;
        private LocalDate date;
        private Double hcp;
}
