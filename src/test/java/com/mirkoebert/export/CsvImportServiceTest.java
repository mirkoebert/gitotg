package com.mirkoebert.export;

import com.mirkoebert.TestSuite;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.mirkoebert.sgi.calc.PointsToSgiHcpFunction;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static com.mirkoebert.export.CsvImportService.MAX_CSV_LINES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CsvImportServiceTest {

    private static final String TEST_USER = "csv-import-test-user";
    @Autowired
    private CsvImportService cut;
    @Autowired
    private HcpRepository hcpRepository;
    @Autowired
    private SingleTestResultRepository singleTestResultRepository;
    @Autowired
    private GMetricRepository gMetricRepository;
    @Autowired
    private PointsToSgiHcpFunction pointsToSgiHcpFunction;

    @BeforeEach
    void cleanup() {
        hcpRepository.findByUserId(TEST_USER).forEach(hcpRepository::delete);
        singleTestResultRepository.findAllByUserId(TEST_USER).forEach(singleTestResultRepository::delete);
        gMetricRepository.findByUserId(TEST_USER).forEach(gMetricRepository::delete);
    }

    // HCP tests

    @SneakyThrows
    @Test
    void importHcpData_insertsWhenNoExisting() {
        final String csv = "date,hcp\n2025-01-31,25.5\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importHcpData(is, TEST_USER);

        assertThat(count).isEqualTo(1);

        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        HcpScoreEntity saved = all.getFirst();
        assertThat(saved.getUserId()).isEqualTo(TEST_USER);
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(saved.getHcp()).isEqualTo(25.5);
    }

    @SneakyThrows
    @Test
    void importHcpData_removesExistingUserDataBeforeImport() {
        hcpRepository.save(HcpScoreEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .hcp(30.0)
                .build());
        hcpRepository.save(HcpScoreEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 6, 1))
                .hcp(28.0)
                .build());
        assertThat(hcpRepository.findByUserId(TEST_USER)).hasSize(2);

        final String csv = "date,hcp\n2025-01-21,25.5\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importHcpData(is, TEST_USER);

        assertThat(count).isEqualTo(1);
        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getHcp()).isEqualTo(25.5);
        assertThat(all.getFirst().getDate()).isEqualTo(LocalDate.of(2025, 1, 21));
        assertThat(hcpRepository.findByUserIdAndDate(TEST_USER, LocalDate.of(2024, 1, 1))).isEmpty();
    }

    @SneakyThrows
    @Test
    void importHcpData_replacesPreviousImportCompletely() {
        String csv1 = "date,hcp\n2025-01-21,20.0\n2025-02-01,19.0\n";
        cut.importHcpData(new ByteArrayInputStream(csv1.getBytes()), TEST_USER);
        assertThat(hcpRepository.findByUserId(TEST_USER)).hasSize(2);

        String csv2 = "date,hcp\n2025-01-21,25.5\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv2.getBytes());

        int count = cut.importHcpData(is, TEST_USER);

        assertThat(count).isEqualTo(1);
        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getHcp()).isEqualTo(25.5);
        assertThat(all.getFirst().getDate()).isEqualTo(LocalDate.of(2025, 1, 21));
        assertThat(hcpRepository.findByUserIdAndDate(TEST_USER, LocalDate.of(2025, 2, 1))).isEmpty();
    }

    @SneakyThrows
    @Test
    void importHcpData_skipsInvalidRowsAndCountsOnlyValid() {
        final String csv = "date,hcp\n2025-01-01,\n,25.5\n2025-01-02,30.0\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importHcpData(is, TEST_USER);

        assertThat(count).isEqualTo(1);

        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
    }

    @Test
    void importHcpData_returnsZeroForNoValidRows() {
        // Seed data must still be cleared even when CSV has no valid rows
        hcpRepository.save(HcpScoreEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 3, 1))
                .hcp(22.0)
                .build());

        String csv = "date,hcp\n,25.5\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importHcpData(is, TEST_USER);

        assertThat(count).isEqualTo(0);
        assertThat(hcpRepository.findByUserId(TEST_USER)).isEmpty();
    }

    // SGI tests

    @SneakyThrows
    @Test
    void importSgiData_insertsAndComputesHcpWhenNoExisting() {
        final String csv = "date,points,testId,testType\n2025-01-01,5,1,SGI\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importSgiData(is, TEST_USER);

        assertThat(count).isEqualTo(1);

        List<SingleTestResultEntity> all = singleTestResultRepository.findAllByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        SingleTestResultEntity saved = all.getFirst();
        assertThat(saved.getUserId()).isEqualTo(TEST_USER);
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(saved.getPoints()).isEqualTo(5);
        assertThat(saved.getTestId()).isEqualTo(1);
        assertThat(saved.getTestType()).isEqualTo(TestSuite.SGI);
        // compute expected using real function
        int expectedHcp = pointsToSgiHcpFunction.apply(1, 5);
        assertThat(saved.getHcp()).isEqualTo(expectedHcp);
    }

    @SneakyThrows
    @Test
    void importSgiData_replacesWhenSameDateAndTestIdExists() {
        // first
        String csv1 = "date,points,testId,testType\n2025-01-21,5,1,SGI\n";
        cut.importSgiData(new ByteArrayInputStream(csv1.getBytes()), TEST_USER);

        // second same date+testId, different points
        String csv2 = "date,points,testId,testType\n2025-01-21,4,1,SGI\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv2.getBytes());

        int count = cut.importSgiData(is, TEST_USER);

        assertThat(count).isEqualTo(1);

        List<SingleTestResultEntity> all = singleTestResultRepository.findAllByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        SingleTestResultEntity saved = all.getFirst();
        assertThat(saved.getPoints()).isEqualTo(4); // replaced
        int expectedHcp = pointsToSgiHcpFunction.apply(1, 4);
        assertThat(saved.getHcp()).isEqualTo(expectedHcp);
    }

    @SneakyThrows
    @Test
    void importSgiData_skipsInvalidRows() {
        final String csv = "date,points,testId,testType\n2025-01-01,5,1,SGI\n,5,1,SGI\n2025-01-02,,2,SGI\n2025-01-03,7,,SGI\n2025-01-04,8,4,\n2025-01-05,9,5,SGI\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importSgiData(is, TEST_USER);

        assertThat(count).isEqualTo(2);

        List<SingleTestResultEntity> all = singleTestResultRepository.findAllByUserId(TEST_USER);
        assertThat(all).hasSize(2);
    }

    @SneakyThrows
    @Test
    void importSgiData_returnsZeroForNoValidRows() {
        String csv = "date,points,testId,testType\n,10,1,SGI\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importSgiData(is, TEST_USER);

        assertThat(count).isEqualTo(0);

        List<SingleTestResultEntity> all = singleTestResultRepository.findAllByUserId(TEST_USER);
        assertThat(all).isEmpty();
    }

    @SneakyThrows
    @Test
    void importSgiData_loadsShortGameCsvFromClasspath() {
        SingleTestResultEntity sre = SingleTestResultEntity
                .builder()
                .date(LocalDate.of(2025, 6, 21))
                .hcp(pointsToSgiHcpFunction.apply(1, 2))
                .points(2)
                .userId(TEST_USER)
                .testType(TestSuite.SGI)
                .testId(1)
                .build();
        singleTestResultRepository.save(sre);
        sre.setId(2);
        singleTestResultRepository.save(sre);
        assertThat(singleTestResultRepository.countByUserId(TEST_USER)).isEqualTo(2);

        @Cleanup InputStream is = getClass().getClassLoader().getResourceAsStream("2026-07-23-short-game.csv");
        assertThat(is).isNotNull();

        int count = cut.importSgiData(is, TEST_USER);

        assertThat(count).isEqualTo(12);

        final List<SingleTestResultEntity> all = singleTestResultRepository.findAllByUserId(TEST_USER);
        assertThat(all).hasSize(12);
        assertThat(all).allMatch(e -> e.getUserId().equals(TEST_USER));
        assertThat(all).allMatch(e -> e.getTestType() == TestSuite.SGI);
        assertThat(all).allMatch(e -> e.getHcp() != null);

        // spot-check a row from each date in the fixture
        SingleTestResultEntity july23Test7 = all.stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2026, 7, 23)) && e.getTestId() == 7)
                .findFirst()
                .orElseThrow();
        assertThat(july23Test7.getPoints()).isEqualTo(5);
        assertThat(july23Test7.getHcp()).isEqualTo(pointsToSgiHcpFunction.apply(7, 5));

        SingleTestResultEntity june21Test1 = all.stream()
                .filter(e -> e.getDate().equals(LocalDate.of(2025, 6, 21)) && e.getTestId() == 1)
                .findFirst()
                .orElseThrow();
        assertThat(june21Test1.getPoints()).isEqualTo(2);
        assertThat(june21Test1.getHcp()).isEqualTo(pointsToSgiHcpFunction.apply(1, 2));

        count = cut.importSgiData(is, TEST_USER);
        assertThat(count).isZero();
    }

    // GMetric tests

    @SneakyThrows
    @Test
    void importGMetricData_insertsWhenNoExisting() {
        final String csv = "date,metricValue,type\n2025-01-31,3,LOST_BALLS\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importGMetricData(is, TEST_USER);

        assertThat(count).isEqualTo(1);

        List<GMetricEntity> all = gMetricRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        GMetricEntity saved = all.getFirst();
        assertThat(saved.getUserId()).isEqualTo(TEST_USER);
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(saved.getMetricValue()).isEqualTo(3);
        assertThat(saved.getType()).isEqualTo(GMetricType.LOST_BALLS);
    }

    @SneakyThrows
    @Test
    void importGMetricData_removesExistingUserDataBeforeImport() {
        gMetricRepository.save(GMetricEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .metricValue(99)
                .type(GMetricType.LOST_BALLS)
                .build());
        gMetricRepository.save(GMetricEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 2, 1))
                .metricValue(88)
                .type(GMetricType.BOGEY)
                .build());
        assertThat(gMetricRepository.findByUserId(TEST_USER)).hasSize(2);

        final String csv = "date,metricValue,type\n2025-01-21,5,BOGEY\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importGMetricData(is, TEST_USER);

        assertThat(count).isEqualTo(1);
        List<GMetricEntity> all = gMetricRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getDate()).isEqualTo(LocalDate.of(2025, 1, 21));
        assertThat(all.getFirst().getMetricValue()).isEqualTo(5);
        assertThat(all.getFirst().getType()).isEqualTo(GMetricType.BOGEY);
        assertThat(gMetricRepository
                .findByUserIdAndDateAndType(TEST_USER, LocalDate.of(2024, 1, 1), GMetricType.LOST_BALLS))
                .isEmpty();
    }

    @SneakyThrows
    @Test
    void importGMetricData_replacesPreviousImportCompletely() {
        String csv1 = "date,metricValue,type\n2025-01-21,2,BOGEY\n2025-01-21,1,LOST_BALLS\n";
        cut.importGMetricData(new ByteArrayInputStream(csv1.getBytes()), TEST_USER);
        assertThat(gMetricRepository.findByUserId(TEST_USER)).hasSize(2);

        String csv2 = "date,metricValue,type\n2025-01-21,5,BOGEY\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv2.getBytes());

        int count = cut.importGMetricData(is, TEST_USER);

        assertThat(count).isEqualTo(1);
        List<GMetricEntity> all = gMetricRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getMetricValue()).isEqualTo(5);
        assertThat(all.getFirst().getType()).isEqualTo(GMetricType.BOGEY);
        assertThat(gMetricRepository
                .findByUserIdAndDateAndType(TEST_USER, LocalDate.of(2025, 1, 21), GMetricType.LOST_BALLS))
                .isEmpty();
    }

    @SneakyThrows
    @Test
    void importGMetricData_allowsSameDateDifferentType() {
        String csv = "date,metricValue,type\n2025-01-21,2,BOGEY\n2025-01-21,1,LOST_BALLS\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importGMetricData(is, TEST_USER);

        assertThat(count).isEqualTo(2);
        assertThat(gMetricRepository.findByUserId(TEST_USER)).hasSize(2);
    }

    @SneakyThrows
    @Test
    void importGMetricData_skipsInvalidRows() {
        String csv = "date,metricValue,type\n2025-01-01,3,\n,2,BOGEY\n2025-01-02,4,DOUBLE_BOGEY\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importGMetricData(is, TEST_USER);

        assertThat(count).isEqualTo(1);
        assertThat(gMetricRepository.findByUserId(TEST_USER)).hasSize(1);
    }

    @SneakyThrows
    @Test
    void importGMetricData_loadsFixtureCsvFromClasspath() {
        // Pre-seed data that must be removed by import
        gMetricRepository.save(GMetricEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2020, 1, 1))
                .metricValue(1)
                .type(GMetricType.DOUBLE_BOGEY)
                .build());

        @Cleanup InputStream is = getClass().getClassLoader().getResourceAsStream("2026-07-26-gmetric.csv");
        assertThat(is).isNotNull();

        int count = cut.importGMetricData(is, TEST_USER);

        assertThat(count).isEqualTo(5);

        List<GMetricEntity> all = gMetricRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(5);
        assertThat(all).allMatch(e -> e.getUserId().equals(TEST_USER));
        assertThat(gMetricRepository
                .findByUserIdAndDateAndType(TEST_USER, LocalDate.of(2020, 1, 1), GMetricType.DOUBLE_BOGEY))
                .isEmpty();

        GMetricEntity lostBalls = gMetricRepository
                .findByUserIdAndDateAndType(TEST_USER, LocalDate.of(2026, 7, 26), GMetricType.LOST_BALLS)
                .orElseThrow();
        assertThat(lostBalls.getMetricValue()).isEqualTo(2);

        GMetricEntity bogey = gMetricRepository
                .findByUserIdAndDateAndType(TEST_USER, LocalDate.of(2025, 6, 15), GMetricType.BOGEY)
                .orElseThrow();
        assertThat(bogey.getMetricValue()).isEqualTo(2);

        // Re-import same fixture: still exactly fixture rows
        @Cleanup InputStream again = getClass().getClassLoader().getResourceAsStream("2026-07-26-gmetric.csv");
        assertThat(again).isNotNull();
        int secondCount = cut.importGMetricData(again, TEST_USER);
        assertThat(secondCount).isEqualTo(5);
        assertThat(gMetricRepository.findByUserId(TEST_USER)).hasSize(5);
    }

    @SneakyThrows
    @Test
    void importGMetricData_acceptsUppercaseHeaders() {
        final String csv = "DATE,METRICVALUE,TYPE\n2026-03-01,7,DOUBLE_BOGEY\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importGMetricData(is, TEST_USER);

        assertThat(count).isEqualTo(1);
        GMetricEntity saved = gMetricRepository
                .findByUserIdAndDateAndType(TEST_USER, LocalDate.of(2026, 3, 1), GMetricType.DOUBLE_BOGEY)
                .orElseThrow();
        assertThat(saved.getMetricValue()).isEqualTo(7);
    }

    @Test
    void countLines_countsHeaderAndDataAndAMissingTrailingNewline() {
        assertThat(CsvImportService.countLines(new byte[0])).isZero();
        assertThat(CsvImportService.countLines("date,hcp\n".getBytes())).isEqualTo(1);
        assertThat(CsvImportService.countLines("date,hcp\n2025-01-01,20.0\n".getBytes())).isEqualTo(2);
        assertThat(CsvImportService.countLines("date,hcp\n2025-01-01,20.0".getBytes())).isEqualTo(2);
    }

    @SneakyThrows
    @Test
    void importHcpData_acceptsExactlyMaxLinesIncludingHeader() {
        int dataRows = MAX_CSV_LINES - 1;
        String csv = hcpCsv(dataRows);

        int count = cut.importHcpData(new ByteArrayInputStream(csv.getBytes()), TEST_USER);

        assertThat(count).isEqualTo(dataRows);
        assertThat(hcpRepository.findByUserId(TEST_USER)).hasSize(dataRows);
    }

    @SneakyThrows
    @Test
    void importHcpData_rejectsOverMaxLinesAndKeepsExistingData() {
        hcpRepository.save(HcpScoreEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .hcp(30.0)
                .build());

        String csv = hcpCsv(MAX_CSV_LINES);

        assertThatThrownBy(() -> cut.importHcpData(new ByteArrayInputStream(csv.getBytes()), TEST_USER))
                .isInstanceOf(CsvImportTooManyLinesException.class)
                .extracting(ex -> ((CsvImportTooManyLinesException) ex).getMaxLines())
                .isEqualTo(MAX_CSV_LINES);

        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getHcp()).isEqualTo(30.0);
    }

    @SneakyThrows
    @Test
    void importSgiData_rejectsOverMaxLinesAndKeepsExistingData() {
        singleTestResultRepository.save(SingleTestResultEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .points(5)
                .testId(1)
                .testType(TestSuite.SGI)
                .hcp(31)
                .build());

        String csv = sgiCsv(MAX_CSV_LINES);

        assertThatThrownBy(() -> cut.importSgiData(new ByteArrayInputStream(csv.getBytes()), TEST_USER))
                .isInstanceOf(CsvImportTooManyLinesException.class);

        List<SingleTestResultEntity> all = singleTestResultRepository.findAllByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getPoints()).isEqualTo(5);
    }

    @SneakyThrows
    @Test
    void importGMetricData_rejectsOverMaxLinesAndKeepsExistingData() {
        gMetricRepository.save(GMetricEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .metricValue(2)
                .type(GMetricType.LOST_BALLS)
                .build());

        String csv = gmetricCsv(MAX_CSV_LINES);

        assertThatThrownBy(() -> cut.importGMetricData(new ByteArrayInputStream(csv.getBytes()), TEST_USER))
                .isInstanceOf(CsvImportTooManyLinesException.class);

        List<GMetricEntity> all = gMetricRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getMetricValue()).isEqualTo(2);
    }

    private static String hcpCsv(int dataRows) {
        StringBuilder csv = new StringBuilder("date,hcp\n");
        for (int i = 0; i < dataRows; i++) {
            csv.append("2025-01-").append("%02d".formatted((i % 28) + 1))
                    .append(',').append(20 + (i % 10)).append(".0\n");
        }
        return csv.toString();
    }

    private static String sgiCsv(int dataRows) {
        StringBuilder csv = new StringBuilder("date,points,testId,testType\n");
        for (int i = 0; i < dataRows; i++) {
            csv.append("2025-01-").append("%02d".formatted((i % 28) + 1))
                    .append(',').append(i % 10)
                    .append(',').append((i % 8) + 1)
                    .append(",SGI\n");
        }
        return csv.toString();
    }

    private static String gmetricCsv(int dataRows) {
        StringBuilder csv = new StringBuilder("date,metricValue,type\n");
        for (int i = 0; i < dataRows; i++) {
            csv.append("2025-01-").append("%02d".formatted((i % 28) + 1))
                    .append(',').append(i % 5)
                    .append(",LOST_BALLS\n");
        }
        return csv.toString();
    }

}

