# `pom.xml` — File Guide

## Purpose

This Maven Project Object Model file tells Maven how to identify, compile, test, and run the project. It is the build configuration source of truth; IDE-specific project files should not override its Java version or main-class setting.

## Main Settings and Dependencies

- `groupId`: `com.smartroute` — project namespace.
- `artifactId`: `smart-route-finder` — Maven artifact name.
- `version`: `1.0-SNAPSHOT` — current development version.
- `maven.compiler.release`: `21` — compile using Java 21 language/API compatibility.
- `javafx.version`: `21.0.8` — version used by the JavaFX Controls dependency.
- **Dependencies:**
  - `org.openjfx:javafx-controls`: provides JavaFX controls and canvas rendering.
  - `org.junit.jupiter:junit-jupiter` (5.10.2, scope `test`): provides the JUnit 5 test framework for automated unit testing of graph structures, XML data loaders, and algorithms.
- **Plugins:**
  - `maven-compiler-plugin` (3.13.0): compiles Java source and test code.
  - `javafx-maven-plugin` (0.0.8): launches the JavaFX desktop application with `mvn javafx:run` pointing to `com.smartroute.SmartRouteFinderGUI`.

## How It Is Used

Run these commands from the project root:

```bash
mvn clean test
mvn clean package
mvn clean javafx:run
```

- `mvn clean test`: Compiles and runs all automated unit tests under `src/test/java/`.
- `mvn clean package`: Compiles and packages the application JAR into `target/`.
- `mvn clean javafx:run`: Launches the JavaFX application window.

## Important Maintenance Rules

- Add a dependency only when `plan.md` approves the need; explain its role in this guide and README.
- Standard Java libraries (`java.xml`, `java.net.http`, `java.util`) are preferred over third-party utilities.
- Keep `maven.compiler.release` aligned with Java 21.
- Keep `mainClass` aligned with `com.smartroute.SmartRouteFinderGUI`.

## Verification

The POM XML structure conforms to Maven 4.0.0 schema.
