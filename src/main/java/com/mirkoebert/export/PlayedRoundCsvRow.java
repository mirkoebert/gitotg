package com.mirkoebert.export;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * OpenCSV bean for played-round rows.
 * Import reads {@code DATE}, {@code HOLE_1}…{@code HOLE_9}, and {@code LOST_BALLS}.
 * Export also writes {@code COURSE} and {@code HOLE_10}…{@code HOLE_18}.
 */
@Data
public class PlayedRoundCsvRow {

    @CsvBindByName(column = "DATE")
    @CsvDate("yyyy-MM-dd")
    private LocalDate date;

    @CsvBindByName(column = "COURSE")
    private String course;

    @CsvBindByName(column = "HOLE_1")
    private Integer hole1;
    @CsvBindByName(column = "HOLE_2")
    private Integer hole2;
    @CsvBindByName(column = "HOLE_3")
    private Integer hole3;
    @CsvBindByName(column = "HOLE_4")
    private Integer hole4;
    @CsvBindByName(column = "HOLE_5")
    private Integer hole5;
    @CsvBindByName(column = "HOLE_6")
    private Integer hole6;
    @CsvBindByName(column = "HOLE_7")
    private Integer hole7;
    @CsvBindByName(column = "HOLE_8")
    private Integer hole8;
    @CsvBindByName(column = "HOLE_9")
    private Integer hole9;
    @CsvBindByName(column = "HOLE_10")
    private Integer hole10;
    @CsvBindByName(column = "HOLE_11")
    private Integer hole11;
    @CsvBindByName(column = "HOLE_12")
    private Integer hole12;
    @CsvBindByName(column = "HOLE_13")
    private Integer hole13;
    @CsvBindByName(column = "HOLE_14")
    private Integer hole14;
    @CsvBindByName(column = "HOLE_15")
    private Integer hole15;
    @CsvBindByName(column = "HOLE_16")
    private Integer hole16;
    @CsvBindByName(column = "HOLE_17")
    private Integer hole17;
    @CsvBindByName(column = "HOLE_18")
    private Integer hole18;

    @CsvBindByName(column = "LOST_BALLS")
    private Integer lostBalls;

    void setHole(int number, Integer strokes) {
        switch (number) {
            case 1 -> hole1 = strokes;
            case 2 -> hole2 = strokes;
            case 3 -> hole3 = strokes;
            case 4 -> hole4 = strokes;
            case 5 -> hole5 = strokes;
            case 6 -> hole6 = strokes;
            case 7 -> hole7 = strokes;
            case 8 -> hole8 = strokes;
            case 9 -> hole9 = strokes;
            case 10 -> hole10 = strokes;
            case 11 -> hole11 = strokes;
            case 12 -> hole12 = strokes;
            case 13 -> hole13 = strokes;
            case 14 -> hole14 = strokes;
            case 15 -> hole15 = strokes;
            case 16 -> hole16 = strokes;
            case 17 -> hole17 = strokes;
            case 18 -> hole18 = strokes;
            default -> throw new IllegalArgumentException("hole " + number);
        }
    }

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
