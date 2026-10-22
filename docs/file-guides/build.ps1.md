# `build.ps1` — File Guide

## Purpose

`build.ps1` is a PowerShell script that compiles all main Java sources, copies static resources (`campus-data-metadata.properties` and `vit-vellore-campus.osm.xml`), and compiles all test sources using the locally installed JDK and JavaFX 21 SDK jars.

## Usage

```powershell
.\build.ps1
```

## Behavior

1. Resolves JavaFX SDK module path from the user's local Maven cache (`.m2/repository/org/openjfx`).
2. Creates `target/classes` and `target/test-classes` if they do not exist.
3. Compiles `src/main/java/**/*.java` to `target/classes`.
4. Copies `src/main/resources/*` to `target/classes`.
5. Compiles `src/test/java/**/*.java` to `target/test-classes`.

