---
name: maintenance
description: Used to fix all CVEs in dependencies and to keep the complexity of pom low.
---

# Maven maintenance rules
1. Prefer managed dependency over latest versions, if they are CVE free
2. Propose a plan and wait for my OK. 
3. read pom.xml 
4. read suppress.xml 
5. Update parent to latest stable version 
6. Update all self-managed dependencies to latest stable versions 
7. Remove not needed entries in supress.xml 
8. Update all maven plugins to latest stable versions 
9. Check for vulnerabilities with OWASP checker: `mvn clean test verify -DnvdApiKey=$NVD_API_KEY`, NVD_API_KEY is set 
10. Report all open CVEs 
11. Increase Bugfix Version number
