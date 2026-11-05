Feature: Played rounds this year on the cockpit
  The cockpit shows how many rounds the player has logged this year.

  Scenario: Show value 'Played rounds this year'
    Given I am an authenticated user
    And I have sixteen played rounds for different courses
    When I open the cockpit page
    Then My 'Played rounds this year' value is shown
