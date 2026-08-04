package com.mirkoebert.export;

import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
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
 * Proves importHcpData's @Transactional boundary: if a save fails partway through,
 * the preceding delete-all-old-rows and any already-saved new rows are rolled back
 * together, instead of leaving the user with a half-imported, partially-wiped dataset.
 */
@SpringBootTest
class CsvImportServiceTransactionalRollbackTest {

    private static final String TEST_USER = "csv-import-rollback-user";
    private static final LocalDate POISON_DATE = LocalDate.of(2025, 3, 3);

    @Autowired
    private CsvImportService cut;

    @MockitoSpyBean
    private HcpRepository hcpRepository;

    @Test
    void importHcpData_rollsBackDeleteAndPartialSaves_whenALaterRowFailsToSave() {
        hcpRepository.findByUserId(TEST_USER).forEach(hcpRepository::delete);
        HcpScoreEntity existing = hcpRepository.save(HcpScoreEntity.builder()
                .userId(TEST_USER)
                .date(LocalDate.of(2024, 1, 1))
                .hcp(30.0)
                .build());

        // Fail only the save for the poison row; the row before it succeeds first.
        doThrow(new RuntimeException("simulated DB failure"))
                .when(hcpRepository)
                .save(argThat(e -> e != null && POISON_DATE.equals(e.getDate())));

        String csv = "date,hcp\n2025-01-01,20.0\n" + POISON_DATE + ",21.0\n";
        InputStream is = new ByteArrayInputStream(csv.getBytes());

        assertThatThrownBy(() -> cut.importHcpData(is, TEST_USER))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("HCP CSV import failed");

        // Without @Transactional, the old row would already be gone and the
        // 2025-01-01 row would be sitting there as a half-finished import.
        List<HcpScoreEntity> all = hcpRepository.findByUserId(TEST_USER);
        assertThat(all).containsExactly(existing);
    }
}
