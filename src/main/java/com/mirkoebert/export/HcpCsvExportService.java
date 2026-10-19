package com.mirkoebert.export;

import com.mirkoebert.golfcourse.PlayedRoundEntity;
import com.mirkoebert.golfcourse.PlayedRoundRepository;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.opencsv.CSVWriter;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HcpCsvExportService {

    private static final int PLAYED_ROUND_HOLES = 18;
    private static final List<String> PLAYED_ROUND_COLUMNS = List.of(
            "DATE", "COURSE",
            "HOLE_1", "HOLE_2", "HOLE_3", "HOLE_4", "HOLE_5", "HOLE_6",
            "HOLE_7", "HOLE_8", "HOLE_9", "HOLE_10", "HOLE_11", "HOLE_12",
            "HOLE_13", "HOLE_14", "HOLE_15", "HOLE_16", "HOLE_17", "HOLE_18",
            "LOST_BALLS");

    private final HcpRepository repo;
    private final SingleTestResultRepository sgirepo;
    private final GMetricRepository gMetricRepository;
    private final PlayedRoundRepository playedRoundRepository;

    String exportAllHcpDataToCsv(final String userId) {
        final List<HcpScoreEntity> all = repo.findByUserId(userId);
        return transform2Csv(all);
    }

    String transform2Csv(final List<HcpScoreEntity> all) {
        val w = new StringWriter();
        try (CSVWriter writer = new CSVWriter(w)) {
            StatefulBeanToCsv<HcpScoreEntity> sbc = new StatefulBeanToCsvBuilder<HcpScoreEntity>(writer).build();
            sbc.write(all);
        } catch (Exception e) {
            log.error("Can't generate CSV for HcpScoreEntity", e);
            throw new RuntimeException(e);
        }
        val r = w.toString();
        log.debug("CSV \n {}", r);
        return r;
    }

    String transform2CsvX(final List<SingleTestResultEntity> all) {
        val w = new StringWriter();
        try (CSVWriter writer = new CSVWriter(w)) {
            StatefulBeanToCsv<SingleTestResultEntity> sbc = new StatefulBeanToCsvBuilder<SingleTestResultEntity>(writer).build();
            sbc.write(all);
        } catch (Exception e) {
            log.error("Can't generate CSV for SingleTestResultEntity", e);
            throw new RuntimeException(e);
        }
        val r = w.toString();
        log.debug("CSV \n {}", r);
        return r;
    }

    String exportAllSgiDataToCsv(final String userId) {
        final List<SingleTestResultEntity> all = sgirepo.findAllByUserId(userId);
        return transform2CsvX(all);
    }

    String exportAllGMetricDataToCsv(final String userId) {
        final List<GMetricEntity> all = gMetricRepository.findByUserIdOrderByDateDesc(userId);
        return transformGMetric2Csv(all);
    }

    String transformGMetric2Csv(final List<GMetricEntity> all) {
        val w = new StringWriter();
        try (CSVWriter writer = new CSVWriter(w)) {
            StatefulBeanToCsv<GMetricEntity> sbc = new StatefulBeanToCsvBuilder<GMetricEntity>(writer).build();
            sbc.write(all);
        } catch (Exception e) {
            log.error("Can't generate CSV for GMetricEntity", e);
            throw new RuntimeException(e);
        }
        val r = w.toString();
        log.debug("CSV \n {}", r);
        return r;
    }

    String exportAllPlayedRoundsToCsv(final String userId) {
        final List<PlayedRoundCsvRow> rows = playedRoundRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(PlayedRoundEntity::getDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparingLong(PlayedRoundEntity::getId))
                .map(HcpCsvExportService::toPlayedRoundRow)
                .toList();
        return transformPlayedRounds2Csv(rows);
    }

    private static PlayedRoundCsvRow toPlayedRoundRow(final PlayedRoundEntity entity) {
        PlayedRoundCsvRow row = new PlayedRoundCsvRow();
        row.setDate(entity.getDate());
        row.setCourse(entity.getCourseName());
        row.setLostBalls(entity.getLostBalls());
        List<Integer> strokes = entity.getHoleStrokes();
        if (strokes != null) {
            int limit = Math.min(strokes.size(), PLAYED_ROUND_HOLES);
            for (int i = 0; i < limit; i++) {
                row.setHole(i + 1, strokes.get(i));
            }
        }
        return row;
    }

    String transformPlayedRounds2Csv(final List<PlayedRoundCsvRow> rows) {
        val w = new StringWriter();
        HeaderColumnNameMappingStrategy<PlayedRoundCsvRow> strategy = new HeaderColumnNameMappingStrategy<>();
        strategy.setType(PlayedRoundCsvRow.class);
        strategy.setColumnOrderOnWrite(Comparator.comparingInt(PLAYED_ROUND_COLUMNS::indexOf));
        try (CSVWriter writer = new CSVWriter(w)) {
            if (rows.isEmpty()) {
                writer.writeNext(strategy.generateHeader(new PlayedRoundCsvRow()));
            } else {
                StatefulBeanToCsv<PlayedRoundCsvRow> sbc = new StatefulBeanToCsvBuilder<PlayedRoundCsvRow>(writer)
                        .withMappingStrategy(strategy)
                        .build();
                sbc.write(rows);
            }
        } catch (Exception e) {
            log.error("Can't generate CSV for PlayedRound", e);
            throw new RuntimeException(e);
        }
        val r = w.toString();
        log.debug("CSV \n {}", r);
        return r;
    }
}
