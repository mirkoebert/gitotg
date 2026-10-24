# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

GITOTG — a small personal Spring Boot webapp for tracking short-game golf practice on the pitching
green: handicap entries, Short Game Index (SGI) test results, and other golf metrics (lost balls, bogeys,
double bogeys), with charts, goals/checklists, and light AI-flavored "advisor" tips. Single-developer, low-scale
personal project (JVM heap, Tomcat threads, and the H2 cache are tuned for a small VPS, not for throughput).

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

# Run the app locally. application.yaml listens on port 80; this overrides it to 8080.
# Dummy OAuth creds from src/test/resources are not used here —
# real Google/GitHub OAuth app credentials are required, see README.md
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080

# Mutation testing (PIT) — declared in pom.xml but bound to no execution, so it only runs on demand
mvn org.pitest:pitest-maven:mutationCoverage

# Quality reports on the Maven site: Surefire, JaCoCo, dependency-check, and Cucumber.
# Cucumber JSON is written during test; maven-cucumber-reporting renders it in pre-site.
# Must be "test site" — JaCoCo, Surefire and Cucumber all read artifacts the test phase wrote,
# and a bare "mvn site" silently omits them. Output: target/site/index.html
mvn test site
```

**`mvn clean install` currently fails.** OWASP dependency-check is bound to `verify` and version 13
rejects an empty NVD API key (`NvdApiException: Invalid API Key, length of 0`). CI is unaffected
because it runs `mvn -B package`, which stops before `verify` — which also means the CVE gate has
never actually run in CI. Use `mvn clean test` until an `nvdApiKey` is configured.

Tests use an in-memory H2 DB (`src/test/resources/application.yaml`) with fake OAuth client credentials —
no real network/OAuth calls happen in tests.

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
- `sgi` — Short Game Index: `SingleTestResultEntity` records raw points for tests 1–8.
  `sgi/calc` holds one `SgiTestNHcpFunction` for test ids 1–5, 7, and 8, converting raw points → an
  HCP-equivalent score. Test 6 has no class of its own; `PointsToSgiHcpFunction` maps `case 5, 6` onto
  test 5's table. An unknown test id returns 99. `sgi/chart` aggregates SGI + HCP together for charting.
- `golfmetric` (gmetric) — simple counted metrics (`GMetricType`: `LOST_BALLS_PER_9HOLES`, `DOUBLE_BOGEY_PLUS`,
  `BOGEY_PLUS`) per date. `LOST_BALLS` is a played-round CSV column, not this enum constant.
- `timeline` — merges HCP, SGI, and GMetric records into one reverse-chronological feed (`GolfType`: `HCP`,
  `SGIHCP`, `COUNT`). `TimelineRange` caps each source query at 30, 100, or all, then limits the merged feed
  to the same count. The page is served by `MainPrimaryController` at `/timeline`.
- `checklist` / `goal` — goal checklists `break100`, `break90`, `break80`, and `alltime`, with per-user
  checked-item progress. Items are **not** listed in Java: `ChecklistCatalog` discovers them from the base
  bundle `messages.properties`, keys shaped `checklist.<goalSlug>.<id>.name` plus an optional `.desc`. Only
  the per-user checkmarks live in the DB (`GolfCheckEntity.checkListItemId` references those ids, so ids must
  stay stable). **Adding an item to an existing goal** means adding the `.name` key (and `.desc` if wanted) to
  `messages.properties` and `messages_de.properties` — no Java change. **Adding a goal** also needs `GoalEnum`,
  nav and title keys, the navbar, `CockpitView`, `CockpitService`, `cockpit.html`, and the related tests.
  `ChecklistCatalogTest` guards German coverage for discovered keys, `KeyParityTest` requires every key in both
  bundles, and `GoalControllerLocalizationIT` renders the page in both languages.
- `advisor` — picks a random localized tip based on how much data the user has and their handicap tier.
  Tips are **not** listed in Java: `AdviceCatalog` discovers them by convention, scanning the base bundle
  `messages.properties` for keys shaped `advisor.<bucket>.<n>`. Buckets are `fresh` / `few` (data-point
  count) plus one per handicap tier — `hh`, `mh`, `lh`, `sfp`, `scratch` — plus `other` (always mixed in).
  A bucket with no keys is simply silent, so `lh`/`sfp`/`scratch` are wired but currently empty.
  **Adding a tip means adding one line to `messages.properties` and `messages_de.properties` — nothing else.**
  `AdviceCatalogTest` guards that every discovered key has a German translation; `KeyParityTest` covers the
  whole bundle either way.
- `export` — CSV for HCP, SGI, GMetric, and played rounds (`HcpCsvExportService`, `CsvImportService`,
  `AllDataCsvExportService`), served by `CsvExportPrimaryRestController`:
  `GET /api/{handicap,sgi,gmetric,golfcourse}/export`, `POST` on the matching `/import` paths, and
  `GET /api/export/all`.
  HCP, SGI, and GMetric imports **fully replace** that user's rows (delete-all-then-insert) inside a
  `@Transactional` method and rethrow on parse failure, so a failed parse can't leave partial data.
  Played-round import replaces that user's rounds, then each saved row calls `CourseService.submitRound`,
  which **appends** bogey, double-bogey, and lost-ball gmetrics. Importing the same file again duplicates
  those metrics.
- `golfcourse` — hole-by-hole scoring for a played round. `GolfCourseCatalog` hardcodes the courses and
  their pars in Java; `PlayedRoundEntity` references a course by **name string**, so renaming a course
  orphans existing rounds. `BogeyPlusCountFunction` and `DoubleBogeyPlusCountFunction` derive gmetric
  counts from a round. `deleteRound` removes the round and leaves those gmetrics in place (accepted).
- `cockpit` — the landing dashboard: aggregates latest HCP, SGI, the three gmetric snapshots, progress
  on all four goal checklists, the last played round and an advisor tip into one `CockpitView` record.
- `user` — `CurrentUser`/`CurrentUserService` (identity) plus `UserPreferenceEntity`/Service (e.g. locale) and
  stats.
- `config` — security filter chain (`WebSecurityConfig`), locale resolution from user prefs
  (`LocaleConfig`/`LocalePreferenceInterceptor`/`LocaleOAuth2SuccessHandler`), request trace-id filter, global
  `MaxUploadSizeExceededException` handling.

**Controller naming convention:** `XxxPrimaryController` is a `@Controller` serving Thymeleaf views;
`XxxPrimaryRestController` is a `@RestController` serving JSON. The exception is `GMetricRestController`
(`/api/gmetric` and `/api/gmetric/chart-data`). Chart data otherwise lives under `/api/**/chart-data`. CSV
export and import for all four datasets live on `CsvExportPrimaryRestController`. Both typically inject
`CurrentUserService` to scope data to the logged-in user.

**Broad `catch (Exception e)` blocks** in `export/`, `handicap/`, `golfmetric/`, and `sgi/chart/` are an
intentional design choice in this codebase (fail soft, log, keep serving the page) — do not flag them as tech
debt or narrow the exception types unless explicitly asked.

**Localization:** `messages.properties` / `messages_de.properties` (English/German), resolved per-user via
`UserPreferenceEntity` and applied through `LocalePreferenceInterceptor` / `LocaleOAuth2SuccessHandler`.
`KeyParityTest` fails when either file is missing a key or a value is empty.

**Testing:** JUnit 5 + Mockito for unit tests (one per service/component, colocated package structure under
`src/test/java`). Cucumber BDD feature tests live in `src/test/resources/features/*.feature` with step
definitions in `src/test/java/com/mirkoebert/cucumber/`, run through the JUnit Platform Suite engine
(`CucumberTest`), wired to the full Spring context (`CucumberSpringConfiguration`).

Thymeleaf expressions are not type-checked and fail silently at runtime, so a template change needs a
test that actually renders the page — see `GoalControllerLocalizationIT`. Two traps there:
`@AutoConfigureMockMvc` is **not** on this classpath (Spring Boot 4.1 moved it to a module the project
does not depend on), so build MockMvc with `MockMvcBuilders.webAppContextSetup(webApplicationContext)`
and mock `CurrentUserService` rather than fighting the OAuth2 filter chain; and locale is switched with
the `lang` request parameter, not `Accept-Language`, because the app uses a `SessionLocaleResolver`.

Surefire's `argLine` carries both the Mockito javaagent and `@{jacocoArgLine}`. Dropping either
silently disables inline mocks or zeroes out coverage while the build stays green.

**Persistence caveat:** `ddl-auto: update` on file-based H2 with no Flyway or Liquibase — nothing is ever
dropped, renamed, or migrated. Renaming an `@Entity`, an `@Enumerated(EnumType.STRING)` constant, or a
checklist item id silently strands existing rows with no migration path. The drift already in the
database is known and accepted; do not re-raise it.

**Build quality gates:** `maven-enforcer-plugin` checks Maven/Java versions plus `requireUpperBoundDeps`
and `banDuplicatePomDependencyVersions` at `validate`. JaCoCo reports coverage during `test`. The last
recorded report is about 71% lines (see `TECH_DEBT.md`); the controllers are the gap. CSV import/export
must name `StandardCharsets.UTF_8` explicitly at every byte/char boundary.

**CI:** GitHub Actions runs `mvn -B package` on push/PR to `main` (`.github/workflows/maven.yml`) plus a
Qodana static-analysis scan on the `qodana.recommended` profile
(`.github/workflows/qodana_code_quality.yml`). That profile is likely to flag the intentional broad
`catch` blocks. `qodana.yaml` has no active `exclude` list yet (the block is a commented placeholder);
add the inspection id there rather than changing the code.
