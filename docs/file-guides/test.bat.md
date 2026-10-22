# `test.bat` — File Guide

## Purpose

`test.bat` is a Windows batch file located at the repository root. It executes the centralized test suite runner (`com.smartroute.AllTestsRunner`) from the Windows Command Prompt or File Explorer.

## Where It Is Used

- **Execution:** Invoked from Windows Command Prompt (`cmd.exe`) in the workspace root:
  ```cmd
  test.bat
  ```
- **Target Application:** Starts `com.smartroute.AllTestsRunner` with JavaFX controls modules enabled, validating all 7 test suites.

## Script Details

- Resolves JavaFX 21 JARs from `%USERPROFILE%\.m2\repository\org\openjfx\...`.
- Points classpath to `"target/classes;target/test-classes"`.
- Exits with status 0 on success, or 1 on failure.

