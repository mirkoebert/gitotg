package com.mirkoebert.cucumber;

import com.mirkoebert.TestSuite;
import com.mirkoebert.sgi.SgiHcpAggregatedService;
import com.mirkoebert.sgi.SgiTestSuiteHcpFunction;
import com.mirkoebert.sgi.SingleTestResultEntity;
import com.mirkoebert.sgi.SingleTestResultRepository;
import com.mirkoebert.sgi.calc.PointsToSgiHcpFunction;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SgiScoringSteps {

    @Autowired
    private PointsToSgiHcpFunction pointsToSgiHcpFunction;

    @Autowired
    private SgiTestSuiteHcpFunction sgiTestSuiteHcpFunction;

    @Autowired
    private SgiHcpAggregatedService sgiHcpAggregatedService;

    @Autowired
    private SingleTestResultRepository singleTestResultRepository;

    private Integer lastComputedHcp;
    private Integer lastSuiteHcp;
    private String userId;

    @Given("a clean SGI user {string}")
    public void aCleanSgiUser(String userId) {
        this.userId = userId;
        singleTestResultRepository.findAllByUserId(userId).forEach(singleTestResultRepository::delete);
    }

    @Given("the player has these latest SGI results:")
    public void thePlayerHasTheseLatestSgiResults(DataTable table) {
        List<Map<String, String>> rows = table.asMaps();
        for (Map<String, String> row : rows) {
            int testId = Integer.parseInt(row.get("testId"));
            int points = Integer.parseInt(row.get("points"));
            Integer hcp = pointsToSgiHcpFunction.apply(testId, points);
            singleTestResultRepository.save(SingleTestResultEntity.builder()
                    .userId(userId)
                    .testId(testId)
                    .points(points)
                    .hcp(hcp)
                    .testType(TestSuite.SGI)
                    .date(LocalDate.of(2026, 1, 1).plusDays(testId))
                    .build());
        }
    }

    @When("the player scores {int} points on SGI test {int}")
    public void thePlayerScoresPointsOnSgiTest(int points, int testId) {
        lastComputedHcp = pointsToSgiHcpFunction.apply(testId, points);
    }

    @When("the player has a total of {int} suite points")
    public void thePlayerHasATotalOfSuitePoints(int points) {
        lastSuiteHcp = sgiTestSuiteHcpFunction.apply(points);
    }

    @When("the suite short-game HCP is calculated for {string}")
    public void theSuiteShortGameHcpIsCalculatedFor(String userId) {
        this.userId = userId;
        lastSuiteHcp = sgiHcpAggregatedService.getLatestSgiHcpAggregated(userId);
    }

    @Then("the short-game HCP for that result is {int}")
    public void theShortGameHcpForThatResultIs(int expectedHcp) {
        assertThat(lastComputedHcp).isEqualTo(expectedHcp);
    }

    @Then("the suite short-game HCP is {int}")
    public void theSuiteShortGameHcpIs(int expectedHcp) {
        assertThat(lastSuiteHcp).isEqualTo(expectedHcp);
    }
}
