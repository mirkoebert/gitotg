Feature: Current eclectic value on the cockpit
  The cockpit shows the eclectic from the last rounds on the home course.

  Scenario: Show eclectic value
    Given I am an authenticated user
    And I have only two played rounds for the same course
    When I open the cockpit page
    Then My eclectic value is shown
