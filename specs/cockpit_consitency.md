# SPEC: Played Rounds this year, count

## Goal
Show how consistent player plays golf is.

## Current behavior
The current cockpit page doesn't show this value.

## Desired behavior
Show the 'Consistency'
Consistency = standard deviation of the last ten played rounds score (total strokes) on the home course

## Out of scope
Don't touch any existing boxes on the cockpit page.

## UI / UX
Show the eclectic value in the same way, like the others, in a box
---------------------------
| Consistency             |
| 2.7                     |
---------------------------

## Technical notes
Affected files
src/main/resources/templates/cockpit.html

endpoints
URL: /cockpit 


## Acceptance criteria
Write first a unit test for calculating the consistency.

Write acceptance test first.

Scenario: Show value 'Consistency'
    Given I am an authenticated user
    And I have sixteen played rounds for the home course with different results
    When I open the cockpit page
    Then My 'Consistency' value is shown
