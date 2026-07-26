Feature: Golf metrics CSV import
  Users can import golf metrics from CSV files.
  Records are upserted by user, date and type.

  Background:
    Given a clean gmetric user "gherkin-gmetric-user"

  Scenario: Import a single lost balls metric
    When the user imports the gmetric CSV:
      """
      date,metricValue,type
      2025-01-31,3,LOST_BALLS
      """
    Then the user has 1 gmetric record
    And a gmetric exists with date "2025-01-31", type "LOST_BALLS" and value 3

  Scenario: Replacing the same date and type updates the value
    Given the gmetric CSV has already been imported:
      """
      date,metricValue,type
      2025-01-21,2,BOGEY
      """
    When the user imports the gmetric CSV:
      """
      date,metricValue,type
      2025-01-21,5,BOGEY
      """
    Then the user has 1 gmetric record
    And a gmetric exists with date "2025-01-21", type "BOGEY" and value 5

  Scenario: Same date with different types creates separate records
    When the user imports the gmetric CSV:
      """
      date,metricValue,type
      2025-01-21,2,BOGEY
      2025-01-21,1,LOST_BALLS
      """
    Then the user has 2 gmetric records

  Scenario: Incomplete rows are ignored
    When the user imports the gmetric CSV:
      """
      date,metricValue,type
      2025-01-01,3,
      ,2,BOGEY
      2025-01-02,4,DOUBLE_BOGEY
      """
    Then the user has 1 gmetric record
    And a gmetric exists with date "2025-01-02", type "DOUBLE_BOGEY" and value 4
