package com.mirkoebert.export;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.mirkoebert.Constants.ME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class HcpCsvExportServiceTest {

        @Autowired
        private HcpCsvExportService cut;

        @Test
        void exportAllHcpDataToCsv() {
                String csv = cut.exportAllHcpDataToCsv(ME);
                assertThat(csv).isNotEmpty().contains("26.0");
        }
}
