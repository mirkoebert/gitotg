Feature: Handicap and short-game trend
  Trends compare the latest score with the average of the three previous scores.
  A change larger than 1 HCP point counts as improving or worsening.

  Background:
    Given a clean scoring user "bdd-trend-user"

  Scenario: Not enough data for a handicap trend
    Given the player has HCP scores:
      | date       | hcp  |
      | 2026-01-01 | 20.0 |
      | 2026-02-01 | 19.0 |
      | 2026-03-01 | 18.0 |
    When the handicap summary is loaded for "bdd-trend-user"
    Then the handicap trend is "not enough data available"

  Scenario: Handicap is improving
    Given the player has HCP scores:
      | date       | hcp  |
      | 2026-01-01 | 24.0 |
      | 2026-02-01 | 23.0 |
      | 2026-03-01 | 22.0 |
      | 2026-04-01 | 20.0 |
    When the handicap summary is loaded for "bdd-trend-user"
    Then the latest handicap is "20.0"
    And the handicap trend is "improving"

  Scenario: Handicap is worsening
    Given the player has HCP scores:
      | date       | hcp  |
      | 2026-01-01 | 18.0 |
      | 2026-02-01 | 17.0 |
      | 2026-03-01 | 16.0 |
      | 2026-04-01 | 20.0 |
    When the handicap summary is loaded for "bdd-trend-user"
    Then the handicap trend is "worsening"

  Scenario: Handicap is stable within one stroke
    Given the player has HCP scores:
      | date       | hcp  |
      | 2026-01-01 | 20.0 |
      | 2026-02-01 | 20.0 |
      | 2026-03-01 | 20.0 |
      | 2026-04-01 | 20.5 |
    When the handicap summary is loaded for "bdd-trend-user"
    Then the handicap trend is "stable"

  Scenario: SGI test trend is improving
    Given the player has SGI results for test 2:
      | date       | hcp |
      | 2026-01-01 | 24  |
      | 2026-02-01 | 22  |
      | 2026-03-01 | 20  |
      | 2026-04-01 | 10  |
    When the SGI trend for test 2 is calculated for "bdd-trend-user"
    Then the SGI trend is "improving"

  Scenario: SGI test trend is stable
    Given the player has SGI results for test 1:
      | date       | hcp |
      | 2026-01-01 | 15  |
      | 2026-02-01 | 15  |
      | 2026-03-01 | 15  |
      | 2026-04-01 | 15  |
    When the SGI trend for test 1 is calculated for "bdd-trend-user"
    Then the SGI trend is "stable"
