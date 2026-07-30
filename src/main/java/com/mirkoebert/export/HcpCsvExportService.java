package com.mirkoebert.export;

import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HcpCsvExportService {

    private final HcpRepository repo;
    private final SingleTestResultRepository sgirepo;
    private final GMetricRepository gMetricRepository;

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
}
