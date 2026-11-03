Feature: Metrics page showing metrics
  The metrics page charts lost balls, double bogey+, bogey+, handicap, and eclectic and lets the player switch the time range.

  @metrics-page
  Scenario: Show metric page
    Given I am authenticated for the metrics page
    And I have only twenty played rounds for the same course over three years
    When I open the metrics page
    Then My metrics chart is shown
    And The selected time range is 'last year'
    And I can switch the time range to 'last two years' and 'all'
    And The chart updates dynamically when the time range is changed
    And The chart is responsive and works on mobile and desktop
    And Each metric is displayed with a distinct color
    And The tooltip shows exact values when hovering over data points
    And The chart contains all metric types: Lost Balls, Double Bogey+, Bogey+, Handicap, Eclectic
