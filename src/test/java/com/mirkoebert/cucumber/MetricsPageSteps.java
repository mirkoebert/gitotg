package com.mirkoebert.cucumber;

import com.mirkoebert.playedrounds.CourseService;
import com.mirkoebert.playedrounds.PlayedRoundRepository;
import com.mirkoebert.golfmetric.GMetricRepository;
import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MetricsPageSteps {

    private static final String USER_ID = "gherkin-metrics-page-user";
    private static final List<Integer> FISCHLAND_STROKES = List.of(6, 5, 3, 6, 4, 5, 5, 4, 5);
    private static final Pattern BORDER_COLOR = Pattern.compile("'rgba\\((\\d+), (\\d+), (\\d+), 1\\)'");

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private CourseService courseService;
    @Autowired
    private PlayedRoundRepository playedRoundRepository;
    @Autowired
    private GMetricRepository gMetricRepository;

    private MockMvc mockMvc;
    private String pageHtml;
    private String lastYearChart;
    private String lastTwoYearsChart;
    private String allChart;

    @After
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Given("I am authenticated for the metrics page")
    public void iAmAuthenticatedForTheMetricsPage() {
        playedRoundRepository.findByUserId(USER_ID).forEach(playedRoundRepository::delete);
        gMetricRepository.findByUserId(USER_ID).forEach(gMetricRepository::delete);

        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", USER_ID, "name", "Metrics Golfer"),
                "sub");
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "google"));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Given("I have only twenty played rounds for the same course over three years")
    public void iHaveOnlyTwentyPlayedRoundsForTheSameCourseOverThreeYears() {
        LocalDate first = LocalDate.now().minusYears(3);
        for (int i = 0; i < 20; i++) {
            LocalDate date = first.plusDays(i * (365L * 3) / 19);
            assertThat(courseService.submitRound(USER_ID, "Fischland", date, FISCHLAND_STROKES, 1)).isTrue();
        }
        assertThat(playedRoundRepository.findByUserId(USER_ID)).hasSize(20);
    }

    @When("I open the metrics page")
    public void iOpenTheMetricsPage() throws Exception {
        pageHtml = mockMvc.perform(get("/gmetric").param("lang", "en"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Then("My metrics chart is shown")
    public void myMetricsChartIsShown() {
        assertThat(pageHtml).contains("id=\"lineChart\"");
        assertThat(pageHtml).contains("type: 'line'");
        assertThat(pageHtml).contains("beginAtZero: true");
    }

    @Then("The selected time range is 'last year'")
    public void theSelectedTimeRangeIsLastYear() {
        assertThat(pageHtml).contains("class=\"btn btn-outline-primary active\" data-range=\"lastYear\"");
        assertThat(pageHtml).contains("currentRange = 'lastYear'");
        assertThat(pageHtml).doesNotContain("active\" data-range=\"lastTwoYears\"");
        assertThat(pageHtml).doesNotContain("active\" data-range=\"all\"");
    }

    @Then("I can switch the time range to 'last two years' and 'all'")
    public void iCanSwitchTheTimeRangeToLastTwoYearsAndAll() {
        assertThat(pageHtml).contains("data-range=\"lastTwoYears\"");
        assertThat(pageHtml).contains("data-range=\"all\"");
        assertThat(pageHtml).contains("Last two years");
        assertThat(pageHtml).contains("All");
        assertThat(pageHtml).contains("selectRange");
    }

    @Then("The chart updates dynamically when the time range is changed")
    public void theChartUpdatesDynamicallyWhenTheTimeRangeIsChanged() throws Exception {
        assertThat(pageHtml).contains("chartDataUrl");
        assertThat(pageHtml).contains("'range=' + encodeURIComponent(range)");
        assertThat(pageHtml).contains("applyChartData");

        lastYearChart = chartJson("lastYear");
        lastTwoYearsChart = chartJson("lastTwoYears");
        allChart = chartJson("all");

        assertThat(lastTwoYearsChart).isNotEqualTo(lastYearChart);
        assertThat(allChart).isNotEqualTo(lastYearChart);
        assertThat(allChart).isNotEqualTo(lastTwoYearsChart);

        String oldestYear = String.valueOf(LocalDate.now().minusYears(3).getYear());
        assertThat(allChart).contains(oldestYear);
        assertThat(lastYearChart).doesNotContain(oldestYear);
        assertThat(lastTwoYearsChart).doesNotContain(oldestYear);
    }

    @Then("The chart is responsive and works on mobile and desktop")
    public void theChartIsResponsiveAndWorksOnMobileAndDesktop() {
        assertThat(pageHtml).contains("width=device-width");
        assertThat(pageHtml).contains("responsive: true");
        assertThat(pageHtml).contains("width: 100%");
        assertThat(pageHtml).contains("class=\"chart-wrap\"");
        assertThat(pageHtml).doesNotContain("col-lg-8");
        assertThat(pageHtml).doesNotContain("col-md-10");
    }

    @Then("Each metric is displayed with a distinct color")
    public void eachMetricIsDisplayedWithADistinctColor() {
        Matcher matcher = BORDER_COLOR.matcher(pageHtml);
        List<String> colors = new ArrayList<>();
        while (matcher.find()) {
            colors.add(matcher.group(1) + "," + matcher.group(2) + "," + matcher.group(3));
        }
        assertThat(colors).hasSize(5).doesNotHaveDuplicates();
        assertThat(pageHtml).contains("data.handicap");
        assertThat(pageHtml).contains("data.eclectic");
    }

    @Then("The tooltip shows exact values when hovering over data points")
    public void theTooltipShowsExactValuesWhenHoveringOverDataPoints() {
        // Chart.js default tooltips show parsed values; aggregator already rounds to 1 decimal
        assertThat(pageHtml).contains("chart.js");
        assertThat(pageHtml).doesNotContain("toFixed");
        assertThat(pageHtml).doesNotContain("exactTooltipLabel");
    }

    @Then("The chart contains all metric types: Lost Balls, Double Bogey+, Bogey+, Handicap, Eclectic")
    public void theChartContainsAllMetricTypes() throws Exception {
        assertThat(pageHtml).contains("Lost Balls");
        assertThat(pageHtml).contains("Double Bogey+");
        assertThat(pageHtml).contains("Bogey+");
        assertThat(pageHtml).contains("Handicap");
        assertThat(pageHtml).contains("Eclectic");
        assertThat(pageHtml).contains("labelEclectic");
        assertThat(pageHtml).contains("data.eclectic");

        String chart = lastYearChart != null ? lastYearChart : chartJson("lastYear");
        assertThat(chart).contains("\"eclectic\"");
        assertThat(chart).containsPattern("\"eclectic\"\\s*:\\s*\\[[^\\]]*[0-9]");
    }

    private String chartJson(String range) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/gmetric/chart-data").param("range", range))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.lostBalls").isArray())
                .andExpect(jsonPath("$.doubleBogey").isArray())
                .andExpect(jsonPath("$.bogey").isArray())
                .andExpect(jsonPath("$.handicap").isArray())
                .andExpect(jsonPath("$.eclectic").isArray())
                .andReturn();
        return result.getResponse().getContentAsString();
    }
}
