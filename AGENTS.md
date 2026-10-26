# AGENTS.md

## Project overview
GITOTG — a small personal Spring Boot webapp for tracking short-game golf practice on the pitching green: handicap entries, 
Short Game Index (SGI) test results, and other golf metrics (lost balls, bogeys, double bogeys), with charts, 
goals/checklists, and light AI-flavored "advisor" tips. Single-developer, 
low-scale personal project (JVM heap, Tomcat threads, and the H2 cache are tuned for a small VPS, not for throughput).
- Spring Boot 4, Java 25, Maven, H2, Thymeleaf.
- Package root: `com.mirkoebert`.



## Commands
Prefer `mvnd` (Maven Daemon) over `mvn` 
- Build and unit tests: `mvn -q test`
- Full verification (unit + integration): `mvn -q verify`
- Single test class: `mvn -q test -Dtest=HcpServiceTest`


## Architecture
- Layers: `web` -> `service` -> `repository`.

## Code style
- Constructor injection only, no field injection.
- Prefer immutable types: records for DTOs and view models, `final` fields.

## Testing
- Test-driven: write or update a failing test first, then implement.
- Unit tests: JUnit 5 + AssertJ + Mockito. Naming: `shouldDoXWhenY`.
- **Never modify or delete an existing test to make it pass.**
  If a test seems wrong, stop and ask.
- Every bug fix needs a regression test.

## Dependencies and configuration
- No secrets in the repo. Configuration via `application-*.yml` and env vars.

## Git workflow
- Work on a feature branch, never on `main`.
- Small commits, imperative messages: "Add status filter to order list".
- Do not push, rebase, or force-push unless I ask.

## Boundaries
- Do not touch: `docs/`, `.github/workflows/`, generated code in `target/`.
- Do not refactor code outside the scope of the current task.
  Mention improvement ideas at the end instead of implementing them.

## How to work
1. For anything larger than a small fix: propose a plan first and wait for approval.
2. Ask when requirements are unclear or conflict. Do not guess.
3. Run `mvn -q verify` before declaring a task done.
4. At the end, report: what changed, what you verified, assumptions made, open questions.

