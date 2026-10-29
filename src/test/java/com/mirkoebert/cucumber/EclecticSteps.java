package com.mirkoebert.cucumber;

import com.mirkoebert.golfcourse.CourseService;
import com.mirkoebert.golfcourse.PlayedRoundRepository;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EclecticSteps {

    private static final String USER_ID = "gherkin-eclectic-user";

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private CourseService courseService;
    @Autowired
    private PlayedRoundRepository playedRoundRepository;

    private MockMvc mockMvc;
    private String cockpitHtml;

    @After
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Given("I am an authenticated user")
    public void iAmAnAuthenticatedUser() {
        playedRoundRepository.findByUserId(USER_ID).forEach(playedRoundRepository::delete);
        OAuth2User oauth2User = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("sub", USER_ID, "name", "Eclectic Golfer"),
                "sub");
        SecurityContextHolder.getContext().setAuthentication(
                new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "google"));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Given("I have only two played rounds for the same course")
    public void iHaveOnlyTwoPlayedRoundsForTheSameCourse() {
        // Fischland par 35. Best hole scores are 6,4,3,5,3,4,4,3,4, so the eclectic is 1.
        assertThat(courseService.submitRound(
                USER_ID, "Fischland", LocalDate.of(2026, 3, 1),
                List.of(6, 5, 4, 6, 4, 5, 5, 4, 5), 0)).isTrue();
        assertThat(courseService.submitRound(
                USER_ID, "Fischland", LocalDate.of(2026, 3, 8),
                List.of(7, 4, 3, 5, 3, 4, 4, 3, 4), 0)).isTrue();
        assertThat(courseService.findRoundsForUser(USER_ID)).hasSize(2);
    }

    @When("I open the cockpit page")
    public void iOpenTheCockpitPage() throws Exception {
        cockpitHtml = mockMvc.perform(get("/cockpit").param("lang", "en"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @Then("My eclectic value is shown")
    public void myEclecticValueIsShown() {
        assertThat(cockpitHtml).contains("Eclectic");
        assertThat(cockpitHtml).contains("Based on last 10 rounds");
        assertThat(cockpitHtml).containsPattern("(?s)Eclectic.{0,400}>\\s*1\\s*<.{0,200}Based on last 10 rounds");
    }
}
