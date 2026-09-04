package com.mirkoebert.cucumber;

import com.mirkoebert.golfcourse.CourseService;
import com.mirkoebert.golfcourse.PlayedRoundEntity;
import com.mirkoebert.golfcourse.PlayedRoundRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class GolfCourseSteps {

    @Autowired
    private CourseService courseService;
    @Autowired
    private PlayedRoundRepository playedRoundRepository;

    private String userId;
    private boolean lastSubmitResult;
    private long lastRoundId;
    private List<PlayedRoundEntity> lastRounds;

    @Given("a clean golfcourse user {string}")
    public void aCleanGolfcourseUser(String userId) {
        this.userId = userId;
        playedRoundRepository.findByUserId(userId).forEach(playedRoundRepository::delete);
    }

    @Given("the user has submitted a round for course {string} on {string} with hole strokes {string} and {int} lost ball(s)")
    @When("the user submits a round for course {string} on {string} with hole strokes {string} and {int} lost ball(s)")
    public void theUserSubmitsARound(String courseName, String date, String holeStrokesCsv, int lostBalls) {
        List<Integer> holeStrokes = Arrays.stream(holeStrokesCsv.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .toList();
        lastSubmitResult = courseService.submitRound(userId, courseName, LocalDate.parse(date), holeStrokes, lostBalls);

        List<PlayedRoundEntity> rounds = courseService.findRoundsForUser(userId);
        if (!rounds.isEmpty()) {
            lastRoundId = rounds.getFirst().getId();
        }
    }

    @Then("the round submission succeeds")
    public void theRoundSubmissionSucceeds() {
        assertThat(lastSubmitResult).isTrue();
    }

    @Then("the round submission fails")
    public void theRoundSubmissionFails() {
        assertThat(lastSubmitResult).isFalse();
    }

    @Then("the user has {int} round(s)")
    public void theUserHasRounds(int expectedCount) {
        assertThat(courseService.findRoundsForUser(userId)).hasSize(expectedCount);
    }

    @Then("the latest round has total strokes {int}")
    public void theLatestRoundHasTotalStrokes(int expectedTotal) {
        List<PlayedRoundEntity> rounds = courseService.findRoundsForUser(userId);
        assertThat(rounds).isNotEmpty();
        assertThat(rounds.getFirst().getTotalStrokes()).isEqualTo(expectedTotal);
    }

    @Then("the latest round has {int} double bogey(s)")
    public void theLatestRoundHasDoubleBogeys(int expectedCount) {
        List<PlayedRoundEntity> rounds = courseService.findRoundsForUser(userId);
        assertThat(rounds).isNotEmpty();
        assertThat(rounds.getFirst().getDoubleBogeysPlus()).isEqualTo(expectedCount);
    }

    @When("the rounds are listed for the user")
    public void theRoundsAreListedForTheUser() {
        lastRounds = courseService.findRoundsForUser(userId);
    }

    @Then("the rounds are ordered by course {string}, {string}")
    public void theRoundsAreOrderedByCourse(String first, String second) {
        assertThat(lastRounds)
                .extracting(PlayedRoundEntity::getCourseName)
                .containsExactly(first, second);
    }

    @When("the user deletes their round for course {string}")
    public void theUserDeletesTheirRoundForCourse(String courseName) {
        PlayedRoundEntity round = courseService.findRoundsForUser(userId).stream()
                .filter(r -> r.getCourseName().equals(courseName))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No round for course " + courseName));
        courseService.deleteRound(userId, round.getId());
    }

    @When("another user {string} attempts to delete that round")
    public void anotherUserAttemptsToDeleteThatRound(String otherUserId) {
        courseService.deleteRound(otherUserId, lastRoundId);
    }
}
