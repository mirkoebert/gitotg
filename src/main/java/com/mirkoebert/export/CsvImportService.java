package com.mirkoebert.export;

import com.mirkoebert.InputLimits;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.mirkoebert.sgi.calc.PointsToSgiHcpFunction;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    /** Maximum CSV records per import, including the header row. */
    public static final int MAX_CSV_LINES = 420;

    private final HcpRepository hcpRepo;
    private final SingleTestResultRepository sgiRepo;
    private final GMetricRepository gMetricRepo;
    private final PointsToSgiHcpFunction pointsToSgiHcpFunction;

    @Transactional
    public int importHcpData(InputStream inputStream, String userId) {
        try (InputStream limited = limitCsvLines(inputStream);
             InputStreamReader reader = new InputStreamReader(limited);
             CSVReader csvReader = new CSVReader(reader)) {

            HeaderColumnNameTranslateMappingStrategy<HcpScoreEntity> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
            strategy.setType(HcpScoreEntity.class);
            Map<String, String> columnMapping = new HashMap<>();
            columnMapping.put("date", "date");
            columnMapping.put("DATE", "date");
            columnMapping.put("hcp", "hcp");
            columnMapping.put("HCP", "hcp");
            strategy.setColumnMapping(columnMapping);

            CsvToBean<HcpScoreEntity> csvToBean = new CsvToBeanBuilder<HcpScoreEntity>(csvReader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<HcpScoreEntity> beans = csvToBean.parse();
            removeAllOldHcpForUser(userId);
            int count = 0;
            for (HcpScoreEntity bean : beans) {
                if (bean.getDate() != null && inRange(bean.getHcp(), InputLimits.HCP_MIN, InputLimits.HCP_MAX)) {
                    bean.setId(0);
                    bean.setUserId(userId);
                    hcpRepo.save(bean);
                    count++;
                }
            }
            log.info("Imported {} HCP records for user {}", count, userId);
            return count;
        } catch (Exception e) {
            throw importFailed(e, userId, "HCP");
        }
    }

    private void removeAllOldHcpForUser(final String userId) {
        final List<HcpScoreEntity> toRemove = hcpRepo.findByUserId(userId);
        log.info("Remove {} old HCP records from db for user {}.", toRemove.size(), userId);
        hcpRepo.deleteAll(toRemove);
    }

    @Transactional
    public int importSgiData(InputStream inputStream, final String userId) {
        try (InputStream limited = limitCsvLines(inputStream);
             InputStreamReader reader = new InputStreamReader(limited);
             CSVReader csvReader = new CSVReader(reader)) {

            HeaderColumnNameTranslateMappingStrategy<SingleTestResultEntity> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
            strategy.setType(SingleTestResultEntity.class);
            Map<String, String> columnMapping = new HashMap<>();
            columnMapping.put("date", "date");
            columnMapping.put("DATE", "date");
            columnMapping.put("points", "points");
            columnMapping.put("POINTS", "points");
            columnMapping.put("testId", "testId");
            columnMapping.put("TESTID", "testId");
            columnMapping.put("testType", "testType");
            columnMapping.put("TESTTYPE", "testType");
            strategy.setColumnMapping(columnMapping);

            CsvToBean<SingleTestResultEntity> csvToBean = new CsvToBeanBuilder<SingleTestResultEntity>(csvReader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<SingleTestResultEntity> sres = csvToBean.parse();
            int count = 0;
            removeAllOldEntitiesForUser(userId);
            for (SingleTestResultEntity sre : sres) {
                if (sre.getDate() != null
                        && inRange(sre.getPoints(), InputLimits.SGI_POINTS_MIN, InputLimits.SGI_POINTS_MAX)
                        && inRange(sre.getTestId(), InputLimits.SGI_TEST_ID_MIN, InputLimits.SGI_TEST_ID_MAX)
                        && sre.getTestType() != null) {
                    sre.setUserId(userId);
                    Integer computedHcp = pointsToSgiHcpFunction.apply(sre.getTestId(), sre.getPoints());
                    sre.setHcp(computedHcp);
                    sgiRepo.save(sre);
                    count++;
                } else {
                    log.warn("Ignore line with incomplete data {}", sre);
                }
            }
            log.info("Imported {} SGI records for user {}", count, userId);
            return count;
        } catch (Exception e) {
            throw importFailed(e, userId, "SGI");
        }
    }

    private void removeAllOldEntitiesForUser(final String userId) {
        final List<SingleTestResultEntity> toRemove = sgiRepo.findAllByUserId(userId);
        log.info("Remove {} old SGI records from db.", toRemove.size());
        sgiRepo.deleteAll(toRemove);
    }

    private void removeAllOldGMetricsForUser(final String userId) {
        final List<GMetricEntity> toRemove = gMetricRepo.findByUserId(userId);
        log.info("Remove {} old gmetric records from db for user {}.", toRemove.size(), userId);
        gMetricRepo.deleteAll(toRemove);
    }

    @Transactional
    public int importGMetricData(InputStream inputStream,final String userId) {
        try (InputStream limited = limitCsvLines(inputStream);
             InputStreamReader reader = new InputStreamReader(limited);
             CSVReader csvReader = new CSVReader(reader)) {

            HeaderColumnNameTranslateMappingStrategy<GMetricEntity> strategy = new HeaderColumnNameTranslateMappingStrategy<>();
            strategy.setType(GMetricEntity.class);
            Map<String, String> columnMapping = new HashMap<>();
            columnMapping.put("date", "date");
            columnMapping.put("DATE", "date");
            columnMapping.put("metricValue", "metricValue");
            columnMapping.put("METRICVALUE", "metricValue");
            columnMapping.put("type", "type");
            columnMapping.put("TYPE", "type");
            strategy.setColumnMapping(columnMapping);

            CsvToBean<GMetricEntity> csvToBean = new CsvToBeanBuilder<GMetricEntity>(csvReader)
                    .withMappingStrategy(strategy)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<GMetricEntity> beans = csvToBean.parse();
            removeAllOldGMetricsForUser(userId);
            int count = 0;
            for (GMetricEntity bean : beans) {
                if (bean.getDate() != null
                        && bean.getType() != null
                        && inRange(bean.getMetricValue(), InputLimits.METRIC_MIN, InputLimits.METRIC_MAX)) {
                    bean.setId(0);
                    bean.setUserId(userId);
                    gMetricRepo.save(bean);
                    count++;
                } else {
                    log.warn("Ignore gmetric line with incomplete data {}", bean);
                }
            }
            log.info("Imported {} gmetric records for user {}", count, userId);
            return count;
        } catch (Exception e) {
            throw importFailed(e, userId, "GMetric");
        }
    }

    private static RuntimeException importFailed(Exception e, String userId, String kind) {
        if (e instanceof CsvImportTooManyLinesException tooMany) {
            throw tooMany;
        }
        log.error("Failed to import {} CSV for user {}", kind, userId, e);
        throw new RuntimeException(kind + " CSV import failed: " + e.getMessage(), e);
    }

    /**
     * Rejects the file before OpenCSV parses it so a too-large import never
     * deletes existing rows. Counts physical lines, including the header.
     */
    static InputStream limitCsvLines(InputStream inputStream) throws IOException {
        byte[] data = inputStream.readAllBytes();
        if (countLines(data) > MAX_CSV_LINES) {
            throw new CsvImportTooManyLinesException(MAX_CSV_LINES);
        }
        return new ByteArrayInputStream(data);
    }

    static boolean inRange(Number value, int min, int max) {
        if (value == null) {
            return false;
        }
        double v = value.doubleValue();
        return v >= min && v <= max;
    }

    static int countLines(byte[] data) {
        if (data.length == 0) {
            return 0;
        }
        int lines = 0;
        for (byte b : data) {
            if (b == '\n') {
                lines++;
            }
        }
        if (data[data.length - 1] != '\n') {
            lines++;
        }
        return lines;
    }
}
