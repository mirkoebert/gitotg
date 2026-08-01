package com.mirkoebert.cucumber;

import com.mirkoebert.TestSuite;
import com.mirkoebert.handicap.HcpRepository;
import com.mirkoebert.handicap.HcpScoreEntity;
import com.mirkoebert.handicap.HcpScoreOutFormatedDTO;
import com.mirkoebert.handicap.HcpService;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.mirkoebert.sgi.TrendService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HcpTrendSteps {

    @Autowired
    private HcpService hcpService;

    @Autowired
    private HcpRepository hcpRepository;

    @Autowired
    private TrendService trendService;

    @Autowired
    private SingleTestResultRepository singleTestResultRepository;

    private String userId;
    private HcpScoreOutFormatedDTO lastHcpSummary;
    private String lastSgiTrend;

    @Given("a clean scoring user {string}")
    public void aCleanScoringUser(String userId) {
        this.userId = userId;
        LocaleContextHolder.setLocale(Locale.ENGLISH);
        hcpRepository.findByUserId(userId).forEach(hcpRepository::delete);
        singleTestResultRepository.findAllByUserId(userId).forEach(singleTestResultRepository::delete);
    }

    @Given("the player has HCP scores:")
    public void thePlayerHasHcpScores(DataTable table) {
        List<Map<String, String>> rows = table.asMaps();
        for (Map<String, String> row : rows) {
            hcpRepository.save(HcpScoreEntity.builder()
                    .userId(userId)
                    .date(LocalDate.parse(row.get("date")))
                    .hcp(Double.parseDouble(row.get("hcp")))
                    .build());
        }
    }

    @Given("the player has SGI results for test {int}:")
    public void thePlayerHasSgiResultsForTest(int testId, DataTable table) {
        List<Map<String, String>> rows = table.asMaps();
        for (Map<String, String> row : rows) {
            singleTestResultRepository.save(SingleTestResultEntity.builder()
                    .userId(userId)
                    .testId(testId)
                    .testType(TestSuite.SGI)
                    .date(LocalDate.parse(row.get("date")))
                    .hcp(Integer.parseInt(row.get("hcp")))
                    .points(0)
                    .build());
        }
    }

    @When("the handicap summary is loaded for {string}")
    public void theHandicapSummaryIsLoadedFor(String userId) {
        this.userId = userId;
        lastHcpSummary = hcpService.findLatestByUserId(userId);
    }

    @When("the SGI trend for test {int} is calculated for {string}")
    public void theSgiTrendForTestIsCalculatedFor(int testId, String userId) {
        this.userId = userId;
        lastSgiTrend = trendService.getTrend(testId, userId);
    }

    @Then("the handicap trend is {string}")
    public void theHandicapTrendIs(String expectedTrend) {
        assertThat(lastHcpSummary.getTrend()).isEqualTo(expectedTrend);
    }

    @Then("the latest handicap is {string}")
    public void theLatestHandicapIs(String expectedHcp) {
        assertThat(lastHcpSummary.getHcp()).isEqualTo(expectedHcp);
    }

    @Then("the SGI trend is {string}")
    public void theSgiTrendIs(String expectedTrend) {
        assertThat(lastSgiTrend).isEqualTo(expectedTrend);
    }
}
