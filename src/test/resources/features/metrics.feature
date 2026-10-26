Feature: Metrics page showing metrics and handicap

  Background:
    Given I am authenticated

  Scenario: Default time selector is "Last year"
    When I open the golf metrics page
    Then the time range "lastYear" should be active by default

  Scenario: Time selector offers all spec-required options
    When I open the golf metrics page
    Then the time range "lastYear" should be available
    And the time range "lastTwoYears" should be available
    And the time range "all" should be available

  Scenario: Every metric has its own distinct color in the chart
    When I open the golf metrics page
    Then the chart should define a color for "Lost Balls"
    And the chart should define a color for "Bogey"
    And the chart should define a color for "Double Bogey"
    And all defined metric colors should be distinct

  Scenario: Chart includes handicap data
    When I open the golf metrics page
    Then the chart should include a "Handicap" series
