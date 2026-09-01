# GITOTG
Webapp to improve your Golf by tracking and supporting your Short Game. The app focus on training on the pitching green.

## Building
![Made with AI Logo](src/main/resources/static/images/made-with-ai.jpg)
**Recommended:** Use [Apache Maven Daemon (mvnd)](https://github.com/apache/maven-mvnd) for much faster builds (especially incremental ones).

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8080
```
## Deployment
Start app with config
```bash
java -jar target/gitotg-0.5.0-SNAPSHOT.jar --spring.profiles.active=local
```

## Testing / Local run
- Set credentials (environment variables)
    - Google OAuth2: `CLIENT_ID`, `CLIENT_SECRET`
    - GitHub OAuth2: `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`
- Create a GitHub OAuth App (Settings → Developer settings → OAuth Apps):
    - Homepage URL: e.g. `http://localhost:8080`
    - Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
- Login paths: `/oauth2/authorization/google`, `/oauth2/authorization/github`
- Note: Google and GitHub users are separate identities (different user ids). Data is not merged across providers.
- Run locally: `mvnd spring-boot:run -Dspring-boot.run.arguments=--server.port=8080` (or use `mvn`)

## CSV Export / Import
- Export: GET `/api/handicap/export` and `/api/sgi/export` (downloads CSV for current user).
- Import: POST `/api/handicap/import`, `/api/sgi/import`, `/api/gmetric/import` with multipart file (replaces all existing records for the current user; max file size 500KB / 420 lines).
- UI: File upload forms with AJAX on Handicap, Short Game Index, and Metrics pages.
- CSV format matches export
