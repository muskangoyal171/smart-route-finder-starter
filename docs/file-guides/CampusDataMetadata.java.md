# `CampusDataMetadata.java` — File Guide

## Purpose

`CampusDataMetadata` is a strongly-typed data transfer object and loader for `campus-data-metadata.properties`. It encapsulates dataset provenance, bounding box bounds, ODbL licensing terms, attribution text, and snapping thresholds.

## Where It Is Used

- **Package:** `com.smartroute.data`
- **Data Repository:** Instantiated by `CampusDataRepository` during startup.
- **UI Attribution:** Supplies the attribution string to JavaFX views.

## Explicit Imports and Purpose

- `java.io.IOException`: Handles resource reading errors.
- `java.io.InputStream`: Reads the properties resource stream from the classpath.
- `java.util.Properties`: Standard Java property parsing.

## Key Methods

| Method | Role & Description |
|---|---|
| `loadFromClasspath()` | Loads `data/campus-data-metadata.properties` via ClassLoader. |
| `getBboxSouth()`, `getBboxWest()`, etc. | Geographic coordinate bounds of the dataset. |
| `getAttributionText()`, `getAttributionUrl()` | Mandatory OpenStreetMap attribution string and link. |
| `getMaxSnapDistanceMeters()` | Maximum permitted threshold for proximity destination snapping (default 35.0m). |

## Viva Discussion Points

- *Why externalize metadata into `.properties` instead of Java constants?*  
  Separation of configuration from code. Updating data or bounding box does not require recompiling Java sources.

