# SPEC: Current Eclectic value for home course

## Goal
Eclectic is an other number that shows the players potential.

## Current behavior
The current cockpit page doesn't show the eclectic value.

## Desired behavior
Show the eclectic value based on the 10 last rounds on the home course, the name of the home course and the PAR value

## Out of scope
Don't touch any existing boxes on the cockpit page.

## UI / UX
Show the eclectic value in the same way, like the others, in a box
---------------------------
| Eclectic                |
| 22                      |
| Home course: CourseName |
| Par: 35                 |
| Based on last 10 rounds |
---------------------------

## Technical notes
Affected files
src/main/resources/templates/cockpit.html

endpoints
URL: /cockpit 

Use existing: 
EclecticService.getEclecticHcpForLast10Rounds
EclecticService.getHomeCourseName
EclecticService.getCoursePar

## Acceptance criteria
Write acceptance test first.

Scenario: Show eclectic value
    Given I am an authenticated user
    And I have only two played rounds for the same course
    When I open the cockpit page
    Then My eclectic value is shown
    And The name of my Home course is shown
    And The PAR value of my Home course is shown
