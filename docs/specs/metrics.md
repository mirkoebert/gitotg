## Feature: Metrics page showing metrics and handicap

### Goal
Show the user a chart with golf metrics (gmetrics) over time. There is a switcher for the time range: All, Last two years, Last year (default)
Metrics are:
- Lost Balls
- Double Bogey+
- Bogey+

## Current behavior
Show the user a chart with golf metrics (gmetrics) over time. There is a switcher for the time range: All, Last year

## Desired behavior
- There is a switcher for the time range: All, Last two years, Last year (default). If I switch the chart is updated
- Responsive design that works on mobile and desktop
- Each metric has a distinct color in the chart
- Default view: shows data from the last year
- Time frame selector with options:
  - 'Last year' (default)
  - 'Last two years'
  - 'All' (entire history)
 
### Context
Spring Boot 4, Java 25, Thymeleaf.
- Templates: `src/main/resources/templates/`, fragments in `fragments/`.
- Metrics include: lost balls, bogeys+, double bogeys+.

## UI / UX
Show the eclectic value in the same way, like the others, in a box
---------------------------
| Navigation              |
---------------------------
| Chart                   |
---------------------------

## Technical notes
Affected files
src/main/resources/templates/gmetric/index.html

endpoints
URL: /gmetric

The metric is aggregated by month (exists). Extend class GMetricMonthAggregator for an additional range (last two years)

- Chart implementation: use existing charting library (no new dependencies)
- Data points should be connected with lines
- Y-axis should show appropriate scale for each metric type
- Tooltip should show exact values when hovering over data points

### Constraints
- No new dependencies

## Acceptance criteria
Write acceptance test first.

Scenario: Show metric page
Given I am an authenticated user
And I have only twenty played rounds for the same course over three years
When I open the metrics page
Then My metrics chart is shown
And The selected time range is 'last year'
