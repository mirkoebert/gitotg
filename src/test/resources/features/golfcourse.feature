Feature: Golf course round tracking
  Users record rounds against a fixed course catalog. A round is only saved when the course
  is known and the number of hole scores matches the course's hole count. Rounds are listed
  most recent first, and can only be deleted by their owner.

  Background:
    Given a clean golfcourse user "gherkin-golfcourse-user"

  Scenario: Submit a round for a known course
    When the user submits a round for course "Fischland" on "2026-01-01" with hole strokes "5,4,2,5,3,4,4,3,4" and 2 lost balls
    Then the round submission succeeds
    And the user has 1 round
    And the latest round has total strokes 34

  Scenario: Double bogeys are counted from hole strokes against par
    When the user submits a round for course "Fischland" on "2026-01-01" with hole strokes "7,4,2,5,5,4,6,3,4" and 0 lost balls
    Then the round submission succeeds
    And the latest round has 3 double bogeys

  Scenario: Reject a round for an unknown course
    When the user submits a round for course "Nonexistent" on "2026-01-01" with hole strokes "5,4,2,5,3,4,4,3,4" and 0 lost balls
    Then the round submission fails
    And the user has 0 rounds

  Scenario: Reject a round whose hole count does not match the course
    When the user submits a round for course "Fischland" on "2026-01-01" with hole strokes "5,4,2" and 0 lost balls
    Then the round submission fails
    And the user has 0 rounds

  Scenario: Rounds are listed most recent first
    Given the user has submitted a round for course "Fischland" on "2026-01-01" with hole strokes "5,4,2,5,3,4,4,3,4" and 0 lost balls
    And the user has submitted a round for course "Tessin" on "2026-02-01" with hole strokes "4,5,3,4,4,5,4,4,3" and 1 lost ball
    When the rounds are listed for the user
    Then the rounds are ordered by course "Tessin", "Fischland"

  Scenario: Deleting your own round removes it
    Given the user has submitted a round for course "Fischland" on "2026-01-01" with hole strokes "5,4,2,5,3,4,4,3,4" and 0 lost balls
    When the user deletes their round for course "Fischland"
    Then the user has 0 rounds

  Scenario: Deleting another user's round has no effect
    Given the user has submitted a round for course "Fischland" on "2026-01-01" with hole strokes "5,4,2,5,3,4,4,3,4" and 0 lost balls
    When another user "gherkin-golfcourse-intruder" attempts to delete that round
    Then the user has 1 round
