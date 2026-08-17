package com.mirkoebert.export;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.mirkoebert.Constants.ME;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AllDataCsvExportServiceTest {

    @Autowired
    private AllDataCsvExportService cut;

    @Test
    void exportAllDataAsZip() throws Exception {
        byte[] zip = cut.exportAllDataAsZip(ME);
        assertThat(zip).isNotEmpty();

        List<String> entryNames = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entryNames.add(entry.getName());
            }
        }

        assertThat(entryNames).hasSize(3);
        assertThat(entryNames).anyMatch(n -> n.endsWith("-handicap.csv"));
        assertThat(entryNames).anyMatch(n -> n.endsWith("-short-game.csv"));
        assertThat(entryNames).anyMatch(n -> n.endsWith("-gmetric.csv"));
    }
}
