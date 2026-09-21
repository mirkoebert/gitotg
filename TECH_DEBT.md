# GITOTG tech debt analysis

**Date:** 2026-09-21  
**Scope:** Full codebase review (Java/Spring Boot app, Maven build, CI, tests, persistence, security).  
**Context:** Personal single-user-scale app on a small VPS. Several “fail soft” patterns are intentional; those are marked **accepted** so they are not treated as cleanup targets unless requirements change.

Coverage numbers below come from the existing JaCoCo report under `target/site/jacoco/` (~71% line coverage: 1,210 of 1,700 lines).

---

## Priority legend

| Priority | Meaning |
|----------|---------|
| **P0** | Breaks local/CI workflows or can corrupt/orphan user data |
| **P1** | Real reliability, security, or maintainability risk at current scale |
| **P2** | Quality gaps that slow change or hide regressions |
| **P3** | Naming, polish, unused code, content gaps |
| **Accepted** | Documented design choice; leave unless goals change |

---

## Executive summary

Open priorities start at enum persistence, course-name coupling, and controller test gaps. Controllers and some aggregators are the main coverage hole. Several domain features (`EclecticByYear`, `UsersGoalEntity`) look unfinished or superseded. CVE-scan/CI friction, `ddl-auto: update` without migrations, and round-delete leaving orphan gmetrics are **accepted** (see below).

---

## P0 — Fix or explicitly gate soon

*None open. Former items 1–3 moved to Accepted on 2026-09-21.*

---

## P1 — Reliability / security / data model

### 4. Enum persistence inconsistency

| Type | Location | Mapping |
|------|----------|---------|
| `GMetricType` | `GMetricEntity` | `@Enumerated(STRING)` |
| `GoalEnum` | `UsersGoalEntity` | default **ORDINAL** |
| `TestSuite` | `SingleTestResultEntity` | default **ORDINAL** |

- ORDINAL breaks if enum order changes. `UsersGoalEntity` also appears unused by current goal UI (see P3), but the table may still exist in the live DB.

### 5. Course identity is a free-form name string

- `GolfCourseCatalog` hardcodes courses in Java.
- `PlayedRoundEntity.courseName` stores the name; renaming a course orphans existing rounds.
- Same string coupling appears in CSV import/export and `EclecticByYear`.

### 6. ~~Dummy data loader can wipe all SGI rows~~ — fixed

- Removed `DummySgiResultGenerator`, `features.load-dummy-data`, and `Constants.ME` (2026-09-21).

### 9. Security filter chain is minimal

- `WebSecurityConfig` only configures auth + OAuth2 + logout.
- No explicit security headers (CSP, HSTS, frame options, referrer policy). Spring Boot defaults help, but nothing is tuned for this app.
- CSRF stays on by default (good for form posts). Delete endpoints use `POST` rather than `DELETE` (noted in code TODO).

### 10. `EclecticByYear` quality issues (even if unused in UI)

- TODO: “two string parameter”.
- `catalog.findByName(...).get()` can throw; broad `catch (Exception)` returns `null`.
- `getEclecticHcpTimeline` iterates years and re-queries/recomputes inefficiently.
- No controller/template wiring found — dead feature surface with tests only.

---

## P2 — Testing and quality gates

### 11. Controller layer largely untested

JaCoCo hotspots (line coverage):

| Class | Approx. coverage |
|-------|------------------|
| `GMetricMonthAggregator` | ~1% |
| `CsvExportPrimaryRestController` | ~2% |
| `SgiPrimaryController` | ~3% |
| `GMetricPrimaryController` | ~4% |
| `HcpPrimaryController` | ~4% |
| `GolfCoursePrimaryController` | ~5% |
| Most other `*Controller` / `*RestController` | low |

Controllers without a dedicated IT: cockpit, CSV export/import REST, gmetric (MVC + REST), HCP (MVC + REST), login, main/timeline, SGI REST. Goal has `GoalControllerLocalizationIT` only.

### 12. Hollow or disabled integration tests

- `SgiPrimaryControllerIT`: empty methods; `submitForm` is `@Disabled` with “TODO fix NPE…”.
- `GolfCoursePrimaryControllerIT.submitForm`: empty body with `// TODO`.
- These green tests add little regression protection.

### 13. Thymeleaf is runtime-only typed

- Template mistakes fail silently at render time.
- Only a few pages have render ITs (`GoalControllerLocalizationIT`, parts of golfcourse/putting-index).
- Changing shared fragments (`navbar`, `head`) has limited automated coverage.

### 14. Mutation testing and Qodana not fully leveraged

- PIT is in the POM with no execution binding; on-demand only.
- `qodana.yaml` has no `exclude` list for intentional broad `catch (Exception)` blocks (project docs already warn Qodana will flag them).

### 15. Package coverage skew

- Strong: `user`, `advisor`, `golfmetric.byyear`, `checklist`, `cockpit` service layer.
- Weak: `golfmetric` controllers/aggregators, root `com.mirkoebert` controllers, most MVC/REST controllers.
- Overall ~71% lines / ~70% instructions / ~61% branches (existing report).

---

## P3 — Naming, dead code, content, polish

### 16. Dead or superseded goal persistence

- `UsersGoalEntity` + `GoalRepository` have no production callers.
- Live goals use `ChecklistService` / `GolfCheckEntity` and message-bundle catalogs.
- Likely leftover from an earlier goals design; table may still exist via `ddl-auto`.

### 17. Naming debt

| Symbol | Issue |
|--------|--------|
| `MyForm` | Opaque name for goal checklist form |
| `SgiTestRepo` | In-memory catalog, not a Spring Data repository |
| Dual routes `/gmetric` and `/golfmetric` | Harmless alias; documents inconsistency |

Typos `HcpScoreOutFormatedDTO` → `HcpScoreOutFormattedDTO` and `getEclecicHcpForYear` → `getEclecticHcpForYear` fixed 2026-09-21.

### 18. SGI test id quirks

- Catalog defines tests 1–8; calc package has functions for 1–5, 7, 8.
- Tests 5 and 6 share `SgiTest5HcpFunction` in `PointsToSgiHcpFunction` (`case 5, 6`).
- Unknown test ids return HCP `99` (magic default).

### 19. Advisor / checklist content gaps

- Advisor buckets `lh` / `sfp` / `scratch` are wired but empty in message bundles (by design: silent if no keys).
- Goal page always returns template `goal/break100` for every slug (works if the template is generic; naming is misleading).

### 20. Docs drift

- README “Building” still leads with `mvn clean install` without the NVD key caveat that `CLAUDE.md` and `deploy/deploy.md` describe.
- CLAUDE.md cites ~78% line coverage; current JaCoCo report is ~71%.

### 21. `spring-boot-starter-tomcat` marked `provided`

- Unusual for an executable JAR deployment style used in `deploy/`. Worth confirming this matches how the service is actually run (embedded vs external Tomcat).

---

## Accepted (do not “fix” unless asked)

| Item | Why accepted |
|------|----------------|
| Broad `catch (Exception)` in export / handicap / golfmetric / sgi chart | Fail soft, log, keep serving; documented in CLAUDE.md |
| File H2 + tiny JVM / Tomcat thread limits | Personal VPS footprint |
| No local password accounts (OAuth only) | Product choice |
| Checklist/advisor tips only in `messages*.properties` | Convention-based catalogs by design |
| Import replaces all user rows for that dataset | Explicit transactional replace semantics |
| **1. CVE scan broken locally / skipped in CI** (`dependency-check` needs `nvdApiKey`; CI runs `package` only) | Personal project; deploy uses `-DnvdApiKey` when needed; CI green path preferred over a hard CVE gate |
| **2. No schema migrations; `ddl-auto: update`** on file H2 | Known drift already accepted in CLAUDE.md; freeze renames; migrate only if product needs change |
| **3. Round delete does not remove derived gmetrics** | Acceptable divergence for this app’s scale; metrics can be edited/deleted via timeline independently |
| **7. H2 credentials hardcoded** (`sa` / `password` in `application.yaml`) | Locked-down personal VPS; file DB is local to the host |
| **8. OAuth identities do not merge** (Google vs GitHub = separate users) | Documented product choice; no cross-provider account linking planned |

### Accepted detail (former open items)

#### 1. CVE scan is broken locally and skipped in CI

- OWASP `dependency-check-maven` 13 is bound to `verify` and rejects an empty `nvdApiKey`.
- CI (`.github/workflows/maven.yml`) runs `mvn -B package`, which stops before `verify`, so the CVE gate never runs.
- README still recommends `mvn clean install`; `deploy/deploy.md` documents `-DnvdApiKey=$NVD_API_KEY`.

#### 2. No schema migrations; `ddl-auto: update` forever

- Production and test both use `spring.jpa.hibernate.ddl-auto: update` on file H2 (`./db/gitotgdb`).
- No Flyway/Liquibase. Renames of entities, enum constants, or checklist item ids strand rows.

#### 3. Deleting a played round does not remove derived metrics

- `CourseService.submitRound` writes `PlayedRoundEntity` **and** `GMetricEntity` rows (bogey+, double bogey+, lost balls).
- `CourseService.deleteRound` deletes only the round; orphan metric points can remain on timeline/charts.

---

## Suggested remediation order

1. **Annotate remaining enums with `EnumType.STRING`** (and plan a one-shot data fix if ORDINAL rows already exist).
2. **Fill or delete hollow ITs**; add MockMvc coverage for HCP/SGI/gmetric/CSV import submit paths (same pattern as `GoalControllerLocalizationIT`).
3. **Remove or wire** `EclecticByYear` and `UsersGoalEntity`/`GoalRepository`.
4. Polish naming and empty advisor buckets as time allows.

---

## Explicit TODOs already in source

| Location | Note |
|----------|------|
| `EclecticByYear.java` | `TODO tech debt two string parameter` |
| `GolfCoursePrimaryController.java` | `TODO why not method delete` (uses POST) |
| `SgiPrimaryControllerIT.java` | `@Disabled` + NPE TODO on submit |
| `GolfCoursePrimaryControllerIT.java` | empty `submitForm` TODO |

---

## Method

Reviewed: package layout, `pom.xml`, `application.yaml`, security config, entities/repos, golfcourse submit/delete path, CSV import/export, CI workflows, Qodana config, JaCoCo report, grep for TODO/FIXME and broad catches, controller vs test mapping, dead-type usage (`UsersGoalEntity`, `EclecticByYear`).

This file is a snapshot for planning; it does not change runtime behavior.
