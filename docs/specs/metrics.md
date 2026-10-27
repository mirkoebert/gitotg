## Feature: Metrics page showing metrics and handicap

### Goal
Show the user a chart with golf metrics (gmetrics) and handicap history.

### Context
Spring Boot 4, Java 25, Thymeleaf.
- Templates: `src/main/resources/templates/`, fragments in `fragments/`.
- Metrics include: lost balls, bogeys, double bogeys, and other golf statistics.

### Behavior
- Default view: shows data from the last year
- Time frame selector with options:
  - 'Last year' (default)
  - 'Last two years'
  - 'All' (entire history)
- Each metric has a distinct color in the chart
- Chart shows both metrics and handicap on the same timeline
- Responsive design that works on mobile and desktop

### Technical Details
- Chart implementation: use existing charting library (no new dependencies)
- Data points should be connected with lines
- Y-axis should show appropriate scale for each metric type
- Tooltip should show exact values when hovering over data points

### Constraints
- No new dependencies
- Must maintain existing performance characteristics
- Chart must be accessible (colorblind-friendly palette)
