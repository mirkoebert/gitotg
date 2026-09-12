package com.mirkoebert.export;

import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenCSV bean for played-round import rows
 * ({@code DATE}, {@code HOLE_1}…{@code HOLE_9}, {@code LOST_BALLS}).
 */
@Data
public class PlayedRoundCsvRow {

    @CsvDate("yyyy-MM-dd")
    private LocalDate date;
    private Integer hole1;
    private Integer hole2;
    private Integer hole3;
    private Integer hole4;
    private Integer hole5;
    private Integer hole6;
    private Integer hole7;
    private Integer hole8;
    private Integer hole9;
    private Integer lostBalls;

    List<Integer> holeStrokesOrNullIfIncomplete() {
        Integer[] holes = {hole1, hole2, hole3, hole4, hole5, hole6, hole7, hole8, hole9};
        List<Integer> strokes = new ArrayList<>(9);
        for (Integer hole : holes) {
            if (hole == null) {
                return null;
            }
            strokes.add(hole);
        }
        return strokes;
    }
}
