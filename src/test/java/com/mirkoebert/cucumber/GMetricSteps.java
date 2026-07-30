package com.mirkoebert.cucumber;

import com.mirkoebert.export.CsvImportService;
import com.mirkoebert.golfmetric.GMetricEntity;
import com.mirkoebert.golfmetric.GMetricRepository;
import com.mirkoebert.golfmetric.GMetricType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GMetricSteps {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private GMetricRepository gMetricRepository;

    private String userId;
    private int lastImportCount;

    @Given("a clean gmetric user {string}")
    public void aCleanGmetricUser(String userId) {
        this.userId = userId;
        gMetricRepository.findByUserId(userId).forEach(gMetricRepository::delete);
    }

    @Given("the gmetric CSV has already been imported:")
    public void theGmetricCsvHasAlreadyBeenImported(String csv) {
        importGmetricCsv(csv);
    }

    @When("the user imports the gmetric CSV:")
    public void theUserImportsTheGmetricCsv(String csv) {
        importGmetricCsv(csv);
    }

    private void importGmetricCsv(String csv) {
        lastImportCount = csvImportService.importGMetricData(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)),
                userId);
    }

    @Then("the user has {int} gmetric record(s)")
    public void theUserHasGmetricRecords(int expectedCount) {
        List<GMetricEntity> all = gMetricRepository.findByUserId(userId);
        assertThat(all).hasSize(expectedCount);
    }

    @Then("the last import saved {int} records")
    public void theLastImportSavedRecords(int expectedCount) {
        assertThat(lastImportCount).isEqualTo(expectedCount);
    }

    @Then("a gmetric exists with date {string}, type {string} and value {int}")
    public void aGmetricExistsWithDateTypeAndValue(String date, String type, int value) {
        LocalDate localDate = LocalDate.parse(date);
        GMetricType metricType = GMetricType.valueOf(type);
        GMetricEntity found = gMetricRepository
                .findByUserIdAndDateAndType(userId, localDate, metricType)
                .orElseThrow(() -> new AssertionError(
                        "No gmetric for user=" + userId + " date=" + date + " type=" + type));
        assertThat(found.getMetricValue()).isEqualTo(value);
    }
}
