package com.mirkoebert.cucumber;

import com.mirkoebert.playedrounds.CourseService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CockpitRoundsSteps {

    private static final List<String> COURSES = List.of("Fischland", "Tessin", "Grambek");
    private static final List<List<Integer>> STROKES = List.of(
            List.of(5, 4, 3, 5, 3, 4, 4, 3, 4),
            List.of(4, 5, 3, 4, 4, 5, 4, 4, 3),
            List.of(4, 5, 3, 4, 4, 4, 3, 4, 4, 4, 3, 4, 5, 4, 5, 4, 3, 4)
    );

    @Autowired
    private CourseService courseService;

    @Given("I have sixteen played rounds for different courses")
    public void iHaveSixteenPlayedRoundsForDifferentCourses() {
        LocalDate firstDay = LocalDate.of(Year.now().getValue(), 1, 1);
        for (int i = 0; i < 16; i++) {
            int course = i % COURSES.size();
            assertThat(courseService.submitRound(
                    EclecticSteps.USER_ID,
                    COURSES.get(course),
                    firstDay.plusDays(i),
                    STROKES.get(course),
                    0)).isTrue();
        }
    }

    @Then("My 'Played rounds this year' value is shown")
    public void myPlayedRoundsThisYearValueIsShown() {
        assertThat(EclecticSteps.cockpitHtml)
                .containsPattern("(?s)Played rounds this year.{0,200}>\\s*16\\s*<");
    }
}
