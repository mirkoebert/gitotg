## Feature: CVE free application

### Goal
Fix all CVE by updating dependencies.

## Behavior
- Update parent to latest stable version
- Update all self-managed dependencies to latest stable versions
- Update all maven plugins to latest stable versions
- Check: `mvn clean test verify -DnvdApiKey=$NVD_API_KEY`

## Constraints
- Prefer managed dependency over latest versions, if they are CVE free

## Additional Notes
- Consider using dependency management tools like OWASP Dependency-Check or Snyk for automated vulnerability scanning
- Document the process for verifying CVE fixes and updating dependencies in a changelog
- Establish a regular schedule for dependency updates (e.g., monthly or quarterly)

## Working agreement
1. Propose a plan and wait for my OK.
