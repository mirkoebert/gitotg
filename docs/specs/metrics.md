## Feature: Metrics page showing metrics and handicap

### Goal
Showing the user a chart with metrics (gmetrics) and handicap

### Context
Spring Boot 3, Java 25, Thymeleaf.
- Templates: `src/main/resources/templates/`, fragments in `fragments/`.

### Behavior
- Default: time selector: last year is selected
- Select time frame of shown data: 'Last year', 'Last two years', 'All'
- Every metric have an own color in the chart

## Constraints
- No new dependencies
