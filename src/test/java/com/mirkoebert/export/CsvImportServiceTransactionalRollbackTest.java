package com.mirkoebert.export;

import com.mirkoebert.TestSuite;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.golfmetric.GMetricType;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

/**
 * Proves each import method's @Transactional boundary: if a save fails partway
 * through, the preceding delete-all-old-rows and any already-saved new rows are
 * rolled back together, instead of leaving the user with a half-imported,
 * partially-wiped dataset.
 */
@SpringBootTest
class CsvImportServiceTransactionalRollbackTest {

    private static final String TEST_USER = "csv-import-rollback-user";

    @Autowired
    private CsvImportService cut;

    @MockitoSpyBean
    private HcpRepository hcpRepository;

    @MockitoSpyBean
    private SingleTestResultRepository sgiRepository;

    @MockitoSpyBean
    private GMetricRepository gMetricRepository;

    @Test
    void importHcpData_rollsBackDeleteAndPartialSaves_whenALaterRowFailsToSave() {
        final LocalDate poisonDate = LocalDate.of(2025, 3, 3);
        hcpRepository.findByUserId(TEST_USER).forEach(hcpRepository::delete);
        HcpScoreEntity existing = hcpRepository.save(HcpScoreEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .hcp(30.0)
                .build());

        // Fail only the save for the poison row; the row before it succeeds first.
        doThrow(new RuntimeException("simulated DB failure"))
                .when(hcpRepository)
                .save(argThat(e -> e != null && poisonDate.equals(e.getDate())));

        String csv = "date,hcp\n2025-01-01,20.0\n" + poisonDate + ",21.0\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        assertThatThrownBy(() -> cut.importHcpData(is, TEST_USER))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("HCP CSV import failed");

        // Without @Transactional, the old row would already be gone and the
        // 2025-01-01 row would be sitting there as a half-finished import.
        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).containsExactly(existing);
    }

    @Test
    void importSgiData_rollsBackDeleteAndPartialSaves_whenALaterRowFailsToSave() {
        final LocalDate poisonDate = LocalDate.of(2025, 4, 4);
        sgiRepository.findAllByUserId(TEST_USER).forEach(sgiRepository::delete);
        SingleTestResultEntity existing = sgiRepository.save(SingleTestResultEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .points(5)
                .testId(1)
                .testType(TestSuite.SGI)
                .hcp(31)
                .build());

        doThrow(new RuntimeException("simulated DB failure"))
                .when(sgiRepository)
                .save(argThat(e -> e != null && poisonDate.equals(e.getDate())));

        String csv = "date,points,testId,testType\n2025-01-01,5,1,SGI\n" + poisonDate + ",6,1,SGI\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        assertThatThrownBy(() -> cut.importSgiData(is, TEST_USER))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SGI CSV import failed");

        List<SingleTestResultEntity> all = sgiRepository.findAllByUserId(TEST_USER);
        assertThat(all).containsExactly(existing);
    }

    @Test
    void importGMetricData_rollsBackDeleteAndPartialSaves_whenALaterRowFailsToSave() {
        final LocalDate poisonDate = LocalDate.of(2025, 5, 5);
        gMetricRepository.findByUserId(TEST_USER).forEach(gMetricRepository::delete);
        GMetricEntity existing = gMetricRepository.save(GMetricEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .metricValue(2)
                .type(GMetricType.LOST_BALLS)
                .build());

        doThrow(new RuntimeException("simulated DB failure"))
                .when(gMetricRepository)
                .save(argThat(e -> e != null && poisonDate.equals(e.getDate())));

        String csv = "date,metricValue,type\n2025-01-01,3,LOST_BALLS\n" + poisonDate + ",4,LOST_BALLS\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        assertThatThrownBy(() -> cut.importGMetricData(is, TEST_USER))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("GMetric CSV import failed");

        List<GMetricEntity> all = gMetricRepository.findByUserId(TEST_USER);
        assertThat(all).containsExactly(existing);
    }
}
