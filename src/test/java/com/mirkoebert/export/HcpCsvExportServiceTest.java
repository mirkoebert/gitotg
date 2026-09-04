package com.mirkoebert.export;

import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HcpCsvExportServiceTest {

    @Autowired
    private HcpCsvExportService cut;

    @Autowired
    private HcpRepository hcpRepository;

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
}
