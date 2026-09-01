# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

GITOTG ("Golf: Improve The Optimal Golf") — a small personal Spring Boot webapp for tracking short-game golf
practice: handicap entries, Short Game Index (SGI) test results, and other golf metrics (lost balls, bogeys,
double bogeys), with charts, goals/checklists, and light AI-flavored "advisor" tips. Single-developer, low-scale
personal project (see the JVM memory tuning throughout — this is optimized to run on a small VPS, not for
throughput).

## Commands

Prefer `mvnd` (Maven Daemon) over `mvn` if installed — much faster incremental builds.

```bash
# Full build + test
mvn clean install

# Run tests only
mvn test

# Run a single test class / method
mvn test -Dtest=HcpServiceTest
mvn test -Dtest=HcpServiceTest#someMethodName

# Run only the Cucumber BDD suite (glue: com.mirkoebert.cucumber, features: src/test/resources/features)
mvn test -Dtest=CucumberTest

# Run the app locally (port 8080, eager bean init, dummy OAuth creds from src/test/resources not used here —
# real Google/GitHub OAuth app credentials are required, see README.md)
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080

# Mutation testing (PIT) and OWASP dependency vulnerability scan are bound as plugins in pom.xml
mvn org.pitest:pitest-maven:mutationCoverage
mvn dependency-check:check
```

Tests use an in-memory H2 DB (`src/test/resources/application.yaml`) with `features.load-dummy-data: true` and
fake OAuth client credentials — no real network/OAuth calls happen in tests.

## Architecture

**Stack:** Spring Boot 4 / Java 25, Thymeleaf for server-rendered pages, a small set of `@RestController`
JSON endpoints for AJAX chart data and CSV import/export, Spring Data JPA over a file-based H2 database
(`./db/gitotgdb`), Spring Security with OAuth2 login only — **there are no local accounts/passwords**, users
authenticate via Google or GitHub.

**Identity model:** `CurrentUser` (`user/CurrentUser.java`) normalizes the different attribute shapes Google
(OIDC: `sub`, `name`, `picture`) and GitHub (`id`, `login`, `avatar_url`) expose as an `OAuth2User`, into one
record. `CurrentUserService` pulls it from the `SecurityContextHolder` per-request. All domain entities key
off a plain `userId` string (the OAuth `sub`/`id`) — there is no local `User` table. **Google and GitHub
identities are separate users**; data does not merge across providers even for the same person.

**Package-by-feature**, one top-level package per domain under `com.mirkoebert`:
- `handicap` (hcp) — golf handicap entries and trend/monthly aggregation.
- `sgi` — Short Game Index: `SingleTestResultEntity` records raw points for one of several short-game tests.
  `sgi/calc` holds one `SgiTestNHcpFunction` per test id (1–5,7,8) converting raw points → an HCP-equivalent
  score; `PointsToSgiHcpFunction` dispatches to the right one by `testId`. `sgi/chart` aggregates SGI + HCP
  together for charting.
- `golfmetric` (gmetric) — simple counted metrics (`GMetricType`: LOST_BALLS, BOGEY, DOUBLE_BOGEY) per date.
- `timeline` — merges HCP, SGI, and GMetric records into one reverse-chronological feed (`GolfType` enum
  discriminates the source); `TimelineRange` caps how much history each source query pulls (30/100/all).
- `checklist` / `goal` — goal-based checklists (e.g. "break 100") with per-user checked-item progress.
  Like the advisor, the items are **not** listed in Java: `ChecklistCatalog` discovers them by convention from
  the base bundle `messages.properties`, keys shaped `checklist.<goalSlug>.<id>.name` plus an optional
  `.desc`. Only the per-user checkmarks live in the DB (`GolfCheckEntity.checkListItemId` references those
  ids, so ids must stay stable). **Adding a checklist item means adding two lines to `messages.properties`
  and `messages_de.properties` — nothing else.** `ChecklistCatalogTest` guards German coverage,
  `GoalControllerLocalizationTest` renders the page in both languages.
- `advisor` — picks a random localized tip based on how much data the user has and their handicap tier.
  Tips are **not** listed in Java: `AdviceCatalog` discovers them by convention, scanning the base bundle
  `messages.properties` for keys shaped `advisor.<bucket>.<n>`. Buckets are `fresh` / `few` (data-point
  count) plus one per handicap tier — `hh`, `mh`, `lh`, `sfp`, `scratch` — plus `other` (always mixed in).
  A bucket with no keys is simply silent, so `lh`/`sfp`/`scratch` are wired but currently empty.
  **Adding a tip means adding one line to `messages.properties` and `messages_de.properties` — nothing else.**
  `AdviceCatalogTest` guards that every discovered key has a German translation.
- `export` — CSV export (`HcpCsvExportService`) and import (`CsvImportService`) for HCP, SGI, and GMetric data.
  **Import always fully replaces existing records for that user** (delete-all-then-insert) within a
  `@Transactional` method, so a failed parse can't leave partial data.
- `user` — `CurrentUser`/`CurrentUserService` (identity) plus `UserPreferenceEntity`/Service (e.g. locale) and
  stats.
- `config` — security filter chain (`WebSecurityConfig`), locale resolution from user prefs
  (`LocaleConfig`/`LocalePreferenceInterceptor`/`LocaleOAuth2SuccessHandler`), request trace-id filter, global
  `MaxUploadSizeExceededException` handling.

**Controller naming convention:** `XxxPrimaryController` is a `@Controller` serving Thymeleaf views;
`XxxPrimaryRestController` is a `@RestController` serving JSON (chart data under `/api/**/chart-data`, CSV
export/import under `/api/**/export|import`). Both typically inject `CurrentUserService` to scope data to the
logged-in user.

**Broad `catch (Exception e)` blocks** in `export/`, `handicap/`, `golfmetric/`, and `sgi/chart/` are an
intentional design choice in this codebase (fail soft, log, keep serving the page) — do not flag them as tech
debt or narrow the exception types unless explicitly asked.

**Localization:** `messages.properties` / `messages_de.properties` (English/German), resolved per-user via
`UserPreferenceEntity` and applied through `LocalePreferenceInterceptor` / `LocaleOAuth2SuccessHandler`.

**Testing:** JUnit 5 + Mockito for unit tests (one per service/component, colocated package structure under
`src/test/java`). Cucumber BDD feature tests live in `src/test/resources/features/*.feature` with step
definitions in `src/test/java/com/mirkoebert/cucumber/`, run through the JUnit Platform Suite engine
(`CucumberTest`), wired to the full Spring context (`CucumberSpringConfiguration`).

**CI:** GitHub Actions runs `mvn -B package` on push/PR to `main` (`.github/workflows/maven.yml`) plus a
Qodana static-analysis scan (`.github/workflows/qodana_code_quality.yml`).
