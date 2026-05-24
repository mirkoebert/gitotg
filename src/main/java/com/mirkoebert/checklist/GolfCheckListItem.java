package com.mirkoebert.checklist;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GolfCheckListItem {

        @Id @GeneratedValue
        private Long id;
        private String name;
        private String description;
        private String goal;

}
