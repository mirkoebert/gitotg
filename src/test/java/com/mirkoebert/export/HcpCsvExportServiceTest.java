package com.mirkoebert.export;

import com.mirkoebert.golfcourse.PlayedRoundEntity;
import com.mirkoebert.golfcourse.PlayedRoundRepository;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HcpCsvExportServiceTest {

    @Autowired
    private HcpCsvExportService cut;

    @Autowired
    private HcpRepository hcpRepository;

    @Autowired
    private PlayedRoundRepository playedRoundRepository;

    @Test
    void exportAllHcpDataToCsv() {
        final String userId = "hcp-export-user";
        hcpRepository.save(HcpScoreEntity.builder()
                .userId(userId)
                .date(LocalDate.of(2025, 9, 7))
                .hcp(26.0)
                .build());

        String csv = cut.exportAllHcpDataToCsv(userId);
        assertThat(csv).isNotEmpty().contains("26.0");
    }

    @Test
    void exportAllPlayedRoundsToCsv_writesEveryHoleAndOnlyThatUser() throws Exception {
        final String userId = "played-round-export-user";
        final String otherUser = "played-round-export-other";
        playedRoundRepository.save(round(userId, LocalDate.of(2026, 4, 2), "Grambek", 1, 18));
        playedRoundRepository.save(round(userId, LocalDate.of(2026, 3, 1), "Fischland", 2, 9));
        playedRoundRepository.save(round(otherUser, LocalDate.of(2026, 3, 1), "Tessin", 9, 9));

        List<String[]> rows = readCsv(cut.exportAllPlayedRoundsToCsv(userId));

        assertThat(rows).hasSize(3);
        assertThat(rows.get(0)).containsExactly(header());
        assertThat(rows.get(1)[0]).isEqualTo("2026-03-01");
        assertThat(rows.get(1)[1]).isEqualTo("Fischland");
        assertThat(rows.get(1)[2]).isEqualTo("4");
        assertThat(rows.get(1)[10]).isEqualTo("12");
        assertThat(rows.get(1)[11]).isEmpty();
        assertThat(rows.get(1)[20]).isEqualTo("2");
        assertThat(rows.get(2)[0]).isEqualTo("2026-04-02");
        assertThat(rows.get(2)[1]).isEqualTo("Grambek");
        assertThat(rows.get(2)[2]).isEqualTo("4");
        assertThat(rows.get(2)[19]).isEqualTo("21");
        assertThat(rows.get(2)[20]).isEqualTo("1");
        assertThat(String.join("\n", rows.stream().map(r -> String.join(",", r)).toList())).doesNotContain("Tessin");
    }

    @Test
    void exportAllPlayedRoundsToCsv_writesHeaderWhenUserHasNoRounds() throws Exception {
        List<String[]> rows = readCsv(cut.exportAllPlayedRoundsToCsv("played-round-export-empty"));
        assertThat(rows).containsExactly(header());
    }

    private static PlayedRoundEntity round(String userId, LocalDate date, String course, int lostBalls, int holes) {
        List<Integer> strokes = IntStream.rangeClosed(1, holes).map(n -> n + 3).boxed().toList();
        return PlayedRoundEntity.builder()
                .userId(userId)
                .date(date)
                .courseName(course)
                .lostBalls(lostBalls)
                .holeStrokes(strokes)
                .build();
    }

    private static String[] header() {
        return new String[]{
                "DATE", "COURSE",
                "HOLE_1", "HOLE_2", "HOLE_3", "HOLE_4", "HOLE_5", "HOLE_6",
                "HOLE_7", "HOLE_8", "HOLE_9", "HOLE_10", "HOLE_11", "HOLE_12",
                "HOLE_13", "HOLE_14", "HOLE_15", "HOLE_16", "HOLE_17", "HOLE_18",
                "LOST_BALLS"
        };
    }

    private static List<String[]> readCsv(String csv) throws Exception {
        try (CSVReader reader = new CSVReader(new StringReader(csv))) {
            return reader.readAll();
        }
    }
}
