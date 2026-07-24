package com.mirkoebert.export;

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CsvImportService {

    private final HcpRepository hcpRepo;
    private final SingleTestResultRepository sgiRepo;
    private final PointsToSgiHcpFunction pointsToSgiHcpFunction;

    public int importHcpData(InputStream inputStream, String userId) {
        try (InputStreamReader reader = new InputStreamReader(inputStream);
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
            int count = 0;
            for (HcpScoreEntity bean : beans) {
                if (bean.getDate() != null && bean.getHcp() != null) {
                    bean.setUserId(userId);
                    hcpRepo.findByUserIdAndDate(userId, bean.getDate()).ifPresent(existing -> bean.setId(existing.getId()));
                    hcpRepo.save(bean);
                    count++;
                }
            }
            log.info("Imported {} HCP records for user {}", count, userId);
            return count;
        } catch (Exception e) {
            log.error("Failed to import HCP CSV for user {}", userId, e);
            throw new RuntimeException("HCP CSV import failed: " + e.getMessage(), e);
        }
    }

    public int importSgiData(InputStream inputStream, final String userId) {
        try (InputStreamReader reader = new InputStreamReader(inputStream);
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

            List<SingleTestResultEntity> beans = csvToBean.parse();
            int count = 0;
            for (SingleTestResultEntity bean : beans) {
                if (bean.getDate() != null && bean.getPoints() != null && bean.getTestId() != null && bean.getTestType() != null) {
                    bean.setUserId(userId);
                    Integer computedHcp = pointsToSgiHcpFunction.apply(bean.getTestId(), bean.getPoints());
                    bean.setHcp(computedHcp);
                    sgiRepo.findByUserIdAndDateAndTestId(userId, bean.getDate(), bean.getTestId()).ifPresent(existing -> bean.setId(existing.getId()));
                    sgiRepo.save(bean);
                    count++;
                }
            }
            log.info("Imported {} SGI records for user {}", count, userId);
            return count;
        } catch (Exception e) {
            log.error("Failed to import SGI CSV for user {}", userId, e);
            throw new RuntimeException("SGI CSV import failed: " + e.getMessage(), e);
        }
    }
}
