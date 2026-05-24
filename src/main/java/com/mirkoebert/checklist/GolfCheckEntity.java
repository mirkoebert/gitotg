package com.mirkoebert.checklist;

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
public class GolfCheckEntity {

        @Id
        @GeneratedValue
        private long id;
        private LocalDate date;
        private String userId;
        private String name;
        private boolean valueX;

}
