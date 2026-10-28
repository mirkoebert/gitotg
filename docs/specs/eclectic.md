# SPEC: Current Year Eclectic value for home course

## Goal
One or two sentences: what problem does this solve, for whom?

## Current behavior
The current cockpit page doesn't show the eclectic value.

## Desired behavior
Show the eclectic value, the year and the count of played rounds that goes in the eclectic calculation

## Out of scope
Don't touch any existing boxes on the cockpit page.

## UI / UX
Show the eclectic value in the same way, like the others, in a box
---------------------------
| Eclectic                |
| 22                      |
| Based on last 10 rounds |
---------------------------

## Technical notes
Affected files
src/main/resources/templates/cockpit.html

endpoints
URL: /cockpit 

Use existing: EclecticService.getEclecticHcpForLast10Rounds

## Acceptance criteria
Scenario: Show eclectic value
    Given I am an authenticated user
    And I have only two played rounds for the same course
    When I open the cockpit page
    Then My eclectic value is shown
