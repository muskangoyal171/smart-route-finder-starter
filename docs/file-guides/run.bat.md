# `run.bat` — File Guide

## Purpose

`run.bat` is a Windows batch file located at the repository root. It allows Windows Command Prompt users to launch the `SmartRouteFinderGUI` JavaFX desktop application with a single command or by double-clicking the file.

## Where It Is Used

- **Execution:** Invoked from Windows Command Prompt (`cmd.exe`) in the workspace root:
  ```cmd
  run.bat
  ```
- **Target Application:** Starts `com.smartroute.SmartRouteFinderGUI` with JavaFX controls modules enabled.

## Script Details

- Resolves JavaFX 21 JARs from `%USERPROFILE%\.m2\repository\org\openjfx\...`.
- Points classpath to `target/classes`.

