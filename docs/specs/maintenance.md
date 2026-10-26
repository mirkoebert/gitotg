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


## Working agreement
1. Propose a plan and wait for my OK.