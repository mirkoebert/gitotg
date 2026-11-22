---
name: coding
description: define some coding guidelines to improve code quality
---

# Write code and tests
- use Java functional interfaces instead of classes with private constructors
- make method parameter final
- final internal objects 
- use Spring stereotypes and dependency injection
- Constructor injection only, no field injection.
- prefer Java streams instead of loops
- add log statements in service methods at the beginning and at the end
- use Lombock annotations
- prefer Java records
- use package by feature approach
- use default access modifier for classes and methods if possible
- at the end: format the code `mvnd spotless:apply`
