package com.mirkoebert.export;

import com.mirkoebert.TestSuite;
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

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CsvImportServiceTest {

    @Autowired
    private CsvImportService cut;

    @Autowired
    private HcpRepository hcpRepository;

    @Autowired
    private SingleTestResultRepository singleTestResultRepository;

    @Autowired
    private PointsToSgiHcpFunction pointsToSgiHcpFunction;

    private static final String TEST_USER = "csv-import-test-user";

    @BeforeEach
    void cleanup() {
        hcpRepository.findByUserId(TEST_USER).forEach(hcpRepository::delete);
        singleTestResultRepository.findAllByUserId(TEST_USER).forEach(singleTestResultRepository::delete);
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
        HcpScoreEntity saved = all.get(0);
        assertThat(saved.getUserId()).isEqualTo(TEST_USER);
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2025, 1, 31));
        assertThat(saved.getHcp()).isEqualTo(25.5);
    }

    @SneakyThrows
    @Test
    void importHcpData_replacesWhenSameDateExists() {
        // first import
        String csv1 = "date,hcp\n2025-01-21,20.0\n";
        cut.importHcpData(new ByteArrayInputStream(csv1.getBytes()), TEST_USER);

        // second import same date, different value -> should replace
        String csv2 = "date,hcp\n2025-01-21,25.5\n";
        @Cleanup InputStream is = new ByteArrayInputStream(csv2.getBytes());

        int count = cut.importHcpData(is, TEST_USER);

        assertThat(count).isEqualTo(1);

        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).hasSize(1);
        HcpScoreEntity saved = all.get(0);
        assertThat(saved.getHcp()).isEqualTo(25.5); // replaced
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2025, 1, 21));
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
        String csv = "date,hcp\n,25.5\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        int count = cut.importHcpData(is, TEST_USER);

        assertThat(count).isEqualTo(0);

        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).isEmpty();
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
        SingleTestResultEntity saved = all.get(0);
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
        SingleTestResultEntity saved = all.get(0);
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
}
