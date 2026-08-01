Feature: Short game index scoring
  Practice test points are converted to a short-game handicap (HCP).
  Better scores mean more points and a lower (or negative tour-level) HCP.

  Scenario Outline: Convert points on a single SGI test to short-game HCP
    When the player scores <points> points on SGI test <testId>
    Then the short-game HCP for that result is <hcp>

    Examples: Known lookup values
      | testId | points | hcp |
      | 1      | 0      | 36  |
      | 1      | 9      | 0   |
      | 2      | 12     | 0   |
      | 3      | 2      | 22  |
      | 4      | 11     | 0   |
      | 5      | 16     | 0   |
      | 6      | 0      | 52  |
      | 6      | 16     | 0   |
      | 7      | 14     | 0   |
      | 8      | 13     | 0   |

    Examples: Tour band and unknown test
      | testId | points | hcp |
      | 1      | 100    | -1  |
      | 8      | 100    | -1  |
      | 0      | 5      | 99  |
      | 9      | 5      | 99  |

  Scenario: Tests 5 and 6 share the same points-to-HCP table
    When the player scores 10 points on SGI test 5
    Then the short-game HCP for that result is 17
    When the player scores 10 points on SGI test 6
    Then the short-game HCP for that result is 17

  Scenario Outline: Convert total suite points to aggregate short-game HCP
    When the player has a total of <points> suite points
    Then the suite short-game HCP is <hcp>

    Examples:
      | points | hcp |
      | 0      | 40  |
      | 11     | 40  |
      | 12     | 39  |
      | 50     | 20  |
      | 110    | 0   |
      | 115    | -1  |
      | 150    | -8  |

  Scenario: Aggregate suite HCP from latest result on each test
    Given a clean SGI user "bdd-sgi-suite-user"
    And the player has these latest SGI results:
      | testId | points |
      | 1      | 8      |
      | 2      | 8      |
      | 3      | 8      |
      | 4      | 8      |
      | 5      | 8      |
      | 6      | 8      |
      | 7      | 8      |
      | 8      | 8      |
    When the suite short-game HCP is calculated for "bdd-sgi-suite-user"
    Then the suite short-game HCP is 14

  Scenario: Missing tests contribute zero points to the suite total
    Given a clean SGI user "bdd-sgi-partial-user"
    And the player has these latest SGI results:
      | testId | points |
      | 1      | 12     |
    When the suite short-game HCP is calculated for "bdd-sgi-partial-user"
    Then the suite short-game HCP is 39
