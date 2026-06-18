# GITOTG
Webapp to improve your Golf by tracking and support your Short Game. The app focus on training on the pitching green.

## Building

**Recommended:** Use [Apache Maven Daemon (mvnd)](https://github.com/apache/maven-mvnd) for much faster builds (especially incremental ones).

```bash
mvnd clean install
mvnd spring-boot:run
```

Plain Maven also works:

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
```

Project Maven configuration lives in `.mvn/` (common flags + JVM tuning for the daemon).

## Testing / Local run
- Set credentials (environment variables)
    - OAuth2
        - `CLIENT_ID`
        - `CLIENT_SECRET`
- Run locally: `mvnd spring-boot:run -Dspring-boot.run.arguments=--server.port=8080` (or use `mvn`)

## CSV Export / Import
- Export: GET `/api/handicap/export` and `/api/sgi/export` (downloads CSV for current user).
- Import: POST `/api/handicap/import` and `/api/sgi/import` with multipart file (appends/replaces records for current user; max file size 1MB).
- UI: File upload forms with AJAX on the Handicap and Short Game Index pages (no page reload on upload).
- CSV format matches export (e.g. columns: date,hcp or date,points,testId,testType). Headers are flexible (case variants supported).
