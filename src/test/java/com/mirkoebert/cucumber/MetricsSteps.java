package com.mirkoebert.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsSteps {

    private static final String METRICS_URL = "/gmetric";

    @Autowired
    private TestRestTemplate restTemplate;

    private String pageHtml;

    @Given("I am authenticated")
    void iAmAuthenticated() {
        OidcUser oidcUser = DefaultOidcUser.withUserAuthority(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        Map.of(
                                "sub", "test-user-123",
                                "name", "Test User",
                                "email", "test@example.com"
                        )
                ).build();
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(oidcUser, null, oidcUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @When("I open the golf metrics page")
    void iOpenTheGolfMetricsPage() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.TEXT_HTML));
        ResponseEntity<String> response = restTemplate.exchange(
                METRICS_URL, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        pageHtml = response.getBody();
        assertThat(pageHtml).isNotBlank();
    }

    @Then("the time range {string} should be active by default")
    void theTimeRangeShouldBeActiveByDefault(String range) {
        Pattern pattern = Pattern.compile(
                "<button[^>]*data-range=[\"']" + Pattern.quote(range) + "[\"'][^>]*class=[\"'][^\"']*active[^\"']*[\"']",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(pageHtml);
        assertThat(matcher.find())
                .as("Button with data-range='%s' should have the 'active' class by default", range)
                .isTrue();
    }

    @Then("the time range {string} should be available")
    void theTimeRangeShouldBeAvailable(String range) {
        Pattern pattern = Pattern.compile(
                "<button[^>]*data-range=[\"']" + Pattern.quote(range) + "[\"']");
        Matcher matcher = pattern.matcher(pageHtml);
        assertThat(matcher.find())
                .as("A button with data-range='%s' should exist on the page", range)
                .isTrue();
    }

    @Then("the chart should define a color for {string}")
    void theChartShouldDefineAColorFor(String metricLabel) {
        // In the template, each metric is defined via smoothDataset(label, borderColor, backgroundColor)
        // We look for the label variable being used in a smoothDataset call with a color argument
        Pattern pattern = Pattern.compile(
                "smoothDataset\\s*\\(\\s*label" + Pattern.quote(camelCase(metricLabel)) +
                "\\s*,\\s*['\"]([^'\"]+)['\"]");
        Matcher matcher = pattern.matcher(pageHtml);
        assertThat(matcher.find())
                .as("Chart should define a color for metric '%s'", metricLabel)
                .isTrue();
    }

    @And("all defined metric colors should be distinct")
    void allDefinedMetricColorsShouldBeDistinct() {
        List<String> metrics = List.of("Lost Balls", "Bogey", "Double Bogey");
        Set<String> colors = new java.util.LinkedHashSet<>();
        for (String metric : metrics) {
            Pattern pattern = Pattern.compile(
                    "smoothDataset\\s*\\(\\s*label" + Pattern.quote(camelCase(metric)) +
                    "\\s*,\\s*['\"]([^'\"]+)['\"]");
            Matcher matcher = pattern.matcher(pageHtml);
            assertThat(matcher.find())
                    .as("Metric '%s' should have a defined color", metric)
                    .isTrue();
            colors.add(matcher.group(1));
        }
        assertThat(colors)
                .as("All metric colors should be distinct, but found: %s", colors)
                .hasSize(metrics.size());
    }

    @Then("the chart should include a {string} series")
    void theChartShouldIncludeASeries(String metricLabel) {
        // Look for the metric label being referenced in the chart configuration
        // Either as a label variable or directly in a dataset definition
        Pattern pattern = Pattern.compile(
                "(?:label" + Pattern.quote(camelCase(metricLabel)) +
                "|['\"]" + Pattern.quote(metricLabel) + "['\"])");
        Matcher matcher = pattern.matcher(pageHtml);
        assertThat(matcher.find())
                .as("Chart should include a series for '%s'", metricLabel)
                .isTrue();
    }

    /**
     * Converts a display label like "Lost Balls" to the JS variable suffix "LostBalls".
     */
    private static String camelCase(String label) {
        String[] parts = label.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return sb.toString();
    }
}
