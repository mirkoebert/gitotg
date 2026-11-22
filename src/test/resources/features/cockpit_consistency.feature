Feature: Consistency on the cockpit
  The cockpit shows the standard deviation of the last ten home-course rounds.

  Scenario: Show value 'Consistency'
    Given I am an authenticated user
    And I have sixteen played rounds for the home course with different results
    When I open the cockpit page
    Then My 'Consistency' value is shown
