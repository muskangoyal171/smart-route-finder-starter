# `test.ps1` — File Guide

## Purpose

`test.ps1` is a convenience PowerShell test runner script located at the repository root. It executes the complete 7-suite automated regression test runner (`com.smartroute.AllTestsRunner`) in one command.

## Where It Is Used

- **Execution:** Invoked from PowerShell in the workspace root:
  ```powershell
  .\test.ps1
  ```
- **Target Application:** Starts `com.smartroute.AllTestsRunner` with JavaFX modules enabled (`--add-modules javafx.controls`), testing model, data, Dijkstra, Prim, Kruskal, vector rendering, and end-to-end UX flows.

## Script Details

- Sets `$ErrorActionPreference = "Stop"`.
- Resolves JavaFX base, graphics, and controls JARs from `$env:USERPROFILE\.m2\repository\org\openjfx\...`.
- Sets classpath to `"target/classes;target/test-classes"`.
- Exits with return code 0 on test success, or code 1 on failure.

