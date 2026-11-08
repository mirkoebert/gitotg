# Golf data analyzer

## Goal: analyze golf data and metrics of the current user to generate a list of weaknesses

## Acceptance criteria
1. If the handicap is below the short game index then the result list contains 'short game'
2. If the short game index is below the short game index then the result list contains 'long game'
3. If 'lost balls' is greater than 1 then the result list contains 'long game' and 'mental game'
4. If the handicap is higher than 20 then the result list contains 'course management', 'mental game'
5. If handicap drops below 30 in the last year then the result list contains 'clubs'
6. If handicap is stable for at least one year then the result list contains 'no progress' 
7. If there are less than 10 played rounds this or last year then the result list contains 'playing'
8. If the short game index have less than 5 entries per test then  the result list contains 'short game'
9. If handicap drops below 18 in the last year then the result list contains 'clubs'
10. If your last played round is more than one month ago then the result list contains 'playing'

## Out of scope
Metric calculation, Metric types, Don't modify any other RestController

## Tasks
- [ ] Create a new Java package 'analytics' for the new code
- [ ] Create a new playwright test for the new 'Analytics' page
- [ ] Create a new page 'Analytics' and put it in the menu under goals
- [ ] Create a test first for the new controller 'AnalyticsRestController'
- [ ] Create a new rest controller that returns the analytics result list
- [ ] Implement