package com.mirkoebert.golfcourse;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayedRoundEntity {

    @Id
    @GeneratedValue
    private long id;
    private String userId;
    private LocalDate date;
    private String courseName;

    @ColumnDefault("0")
    private int lostBalls;

    @ColumnDefault("0")
    private int doubleBogeys;

    @ColumnDefault("0")
    private int bogeys;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "round_hole_strokes", joinColumns = @JoinColumn(name = "round_id"))
    @OrderColumn(name = "hole_number")
    @Column(name = "strokes")
    private List<Integer> holeStrokes;

    public int getTotalStrokes() {
        return holeStrokes == null ? 0 : holeStrokes.stream().mapToInt(Integer::intValue).sum();
    }
}
