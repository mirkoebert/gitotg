## Feature: CVE free application

### Goal
Fix all CVE by updating dependencies.

## Behavior
- Update parent to latest stable version
- Update all self-managed dependencies to latest stable versions
- Update all maven plugins to latest stable versions
- Check for vulnerabilities with OWASP checker: `mvn clean test verify -DnvdApiKey=$NVD_API_KEY`, NVD_API_KEY is set
- Remove not needed entries in supress.xml

## Constraints
- Prefer managed dependency over latest versions, if they are CVE free

## Working agreement
1. read pom.xml
2. read suppress.xml
2. Propose a plan and wait for my OK.
