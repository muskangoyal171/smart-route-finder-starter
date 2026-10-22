# `run.ps1` — File Guide

## Purpose

`run.ps1` is a convenience PowerShell launcher script located at the repository root. It starts the `SmartRouteFinderGUI` JavaFX desktop application using the local Java 26 runtime and JavaFX 21 SDK libraries without requiring manual setup of classpath strings.

## Where It Is Used

- **Execution:** Invoked from PowerShell in the workspace root:
  ```powershell
  .\run.ps1
  ```
- **Target Application:** Starts `com.smartroute.SmartRouteFinderGUI` with JavaFX modules enabled (`--add-modules javafx.controls`).

## Script Details

- Sets `$ErrorActionPreference = "Stop"` for fail-fast behavior.
- Resolves JavaFX base, graphics, and controls JARs from `$env:USERPROFILE\.m2\repository\org\openjfx\...`.
- Points classpath to compiled application classes in `target/classes`.

