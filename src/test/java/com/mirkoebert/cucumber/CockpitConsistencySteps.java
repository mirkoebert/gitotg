package com.mirkoebert.cucumber;

import com.mirkoebert.playedrounds.CourseService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

public class CockpitConsistencySteps {
        private static final String HOME_COURSE = "Fischland";
        private static final int HOLES = 9;
        private static final int[] NEWER_TOTALS = {40, 41, 42, 43, 44, 45, 46, 47, 48, 49};
        @Autowired
        private CourseService courseService;

        @Given("I have sixteen played rounds for the home course with different results")
        public void iHaveSixteenPlayedRoundsForTheHomeCourseWithDifferentResults() {
                LocalDate firstDay = LocalDate.of(2026, 1, 1);
                for (int i = 0; i < 6; i++) {
                        assertThat(submit(firstDay.plusDays(i), 70)).isTrue();
                }
                for (int i = 0; i < NEWER_TOTALS.length; i++) {
                        assertThat(submit(firstDay.plusDays(6L + i), NEWER_TOTALS[i])).isTrue();
                }
        }

        @Then("My 'Consistency' value is shown")
        public void myConsistencyValueIsShown() {
                assertThat(EclecticSteps.cockpitHtml).contains("Consistency");
                assertThat(EclecticSteps.cockpitHtml).containsPattern("(?s)Consistency.{0,200}>\\s*2\\.9\\s*<");
        }

        private boolean submit(LocalDate date, int totalStrokes) {
                return courseService.submitRound(
                                EclecticSteps.USER_ID,
                                HOME_COURSE,
                                date,
                                strokesTotalling(totalStrokes),
                                0
                );
        }

        private static List<Integer> strokesTotalling(int total) {
                int base = total / HOLES;
                int remainder = total % HOLES;
                List<Integer> strokes = new ArrayList<>(HOLES);
                for (int hole = 0; hole < HOLES; hole++) {
                        int strokesOnHole = hole < remainder ? base + 1 : base;
                        assertThat(strokesOnHole).isPositive();
                        strokes.add(strokesOnHole);
                }
                assertThat(strokes.stream().mapToInt(Integer::intValue).sum()).isEqualTo(total);
                return List.copyOf(strokes);
        }
}
