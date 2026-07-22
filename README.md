# GITOTG
Webapp to improve your Golf by tracking and supporting your Short Game. The app focus on training on the pitching green.

## Building
![Made with AI Logo](src/main/resources/static/images/made-with-ai.jpg)
**Recommended:** Use [Apache Maven Daemon (mvnd)](https://github.com/apache/maven-mvnd) for much faster builds (especially incremental ones).

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
```
## Deloypment
Start app with config for reduced memory consumption.
```bash
java -Xms32m -Xmx256m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC -jar target/gitotg-0.2.0-SNAPSHOT.jar --spring.profiles.active=local
```

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
- CSV format matches export
