# SPEC: Played Rounds this year, count

## Goal
Show how intense player plays golf.

## Current behavior
The current cockpit page doesn't show this value.

## Desired behavior
Show the 'Played round this year'

## Out of scope
Don't touch any existing boxes on the cockpit page.

## UI / UX
Show the eclectic value in the same way, like the others, in a box
---------------------------
| Played Round this year  |
| 16                      |
---------------------------

## Technical notes
Affected files
src/main/resources/templates/cockpit.html

endpoints
URL: /cockpit 


## Acceptance criteria
Write acceptance test first.

Scenario: Show value 'Played rounds this year'
    Given I am an authenticated user
    And I have sixteen played rounds for different courses
    When I open the cockpit page
    Then My 'Played rounds this year' value is shown
