# `CampusDataMetadataTest.java` — File Guide

## Purpose

`CampusDataMetadataTest` verifies the parsing, numeric conversions, fallback defaults, and attribution loading in `CampusDataMetadata`.

## Where It Is Used

- **Package:** `com.smartroute.data`
- **Execution:** Executed directly via `AllTestsRunner` or `CampusDataMetadataTest.main()`, or via Maven Surefire test runner.

## Explicit Imports and Purpose

- `com.smartroute.testutil.SimpleAssert`: Test assertion framework.
- `java.io.ByteArrayInputStream`, `java.io.IOException`: Creating in-memory properties input streams.
- `java.nio.charset.StandardCharsets`: UTF-8 character encoding.
- `java.util.Properties`: Java properties container.

## Test Cases Covered

1. `testMetadataParsing()`:
   * Feeds a complete properties block into `CampusDataMetadata`.
   * Asserts exact parsing of source name, export date, bounding box doubles, snap distance, and attribution text.
2. `testMetadataFallbacks()`:
   * Feeds an empty `Properties` object.
   * Asserts that fallback defaults (e.g., bounding box south 12.9640, snap threshold 35.0m) are safely applied without exceptions.

