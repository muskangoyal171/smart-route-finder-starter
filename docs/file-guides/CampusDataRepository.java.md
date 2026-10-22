# `CampusDataRepository.java` — File Guide

## Purpose

`CampusDataRepository` provides access to the loaded `CampusGraph`, selectable campus `Location` instances, and `CampusDataMetadata`. It decouples application controllers and user interface components from raw file I/O and XML stream parsing.

## Where It Is Used

- **Package:** `com.smartroute.data`
- **Application Startup:** Called by JavaFX GUI initialization (`CampusDataRepository.loadDefault()`).
- **Algorithms & UI:** Exposes the active graph for route calculations and the destination list for UI combo boxes.

## Explicit Imports and Purpose

- `com.smartroute.model.CampusGraph`: Root graph model.
- `com.smartroute.model.Location`: Selectable destination model.
- `java.io.IOException`: Error handling during stream reading.
- `java.io.InputStream`: Reading classpath resources.
- `java.util.Collections`, `List`: Unmodifiable collection access.
- `javax.xml.parsers.ParserConfigurationException`, `org.xml.sax.SAXException`: Parsing exceptions.

## Key Methods

| Method | Role & Description |
|---|---|
| `loadDefault()` | Synchronized singleton loader reading `vit-vellore-campus.osm.xml` and metadata from classpath. |
| `getGraph()` | Returns the active `CampusGraph`. |
| `getMetadata()` | Returns the active `CampusDataMetadata`. |
| `getDestinations()` | Returns unmodifiable list of all campus destinations. |
| `findLocation(String name)` | Looks up a destination by case-insensitive name. |
| `reset()` | Clears the singleton instance (used for resetting state between tests). |

## Viva Discussion Points

- *Why use a repository pattern?*  
  It encapsulates data loading in one place. Neither UI controllers nor routing algorithms need to know where the XML file lives or how it is parsed.

