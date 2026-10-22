# `campus-data-metadata.properties` — File Guide

## Purpose

This configuration file records the provenance, licensing, bounding box, filter parameters, and verification statistics for the local OpenStreetMap snapshot `vit-vellore-campus.osm.xml`.

By externalizing these parameters in standard Java `.properties` format, the application loader can display dataset metadata in the UI, verify snapshot consistency, and preserve OpenStreetMap legal attribution obligations.

## Where It Is Used

- **Data Loader:** Loaded by `com.smartroute.data.CampusDataMetadata` (or `OsmXmlCampusDataLoader`) via `ClassLoader.getResourceAsStream("data/campus-data-metadata.properties")`.
- **UI Attribution:** Supplies the attribution string (`attribution.text`) and copyright URL (`attribution.url`) for display in the map interface.
- **Specification Alignment:** Directly satisfies the provenance schema defined in [`docs/osm-data-specification.md`](../osm-data-specification.md).

## Key Properties and Descriptions

### 1. Provenance and Source Keys
* `source.name`: `OpenStreetMap`
* `source.url`: `https://www.openstreetmap.org`
* `source.endpoint`: `https://api.openstreetmap.org/api/0.6/map`
* `source.query.file`: `data/overpass/vit-vellore-campus.overpassql`
* `source.export.date`: Date of snapshot extraction (`2026-10-22`).
* `source.file`: Relative path to raw snapshot (`src/main/resources/data/vit-vellore-campus.osm.xml`).
* `source.file.size_bytes`: Size in bytes (`1537726`).

### 2. Geographic Boundaries
* `bbox.south`: `12.9640000`
* `bbox.west`: `79.1500000`
* `bbox.north`: `12.9790000`
* `bbox.east`: `79.1670000`
* `campus.name`: `Vellore Institute of Technology (VIT), Vellore`
* `campus.location`: `Katpadi, Vellore, Tamil Nadu, India`

### 3. Licensing and Mandatory Attribution
* `licence.name`: `Open Database License (ODbL) 1.0`
* `licence.url`: `https://opendatacommons.org/licenses/odbl/1-0/`
* `attribution.text`: `Map data © OpenStreetMap contributors`
* `attribution.url`: `https://www.openstreetmap.org/copyright`

### 4. Routing and Snapping Parameters
* `filter.allowed_highways`: `footway,path,pedestrian,steps,service,residential,living_street,crossing,unclassified,track`
* `filter.excluded_foot`: `no,private`
* `filter.excluded_access`: `no`
* `destination.max_snap_distance_meters`: `50.0`

### 5. Verified Data Metrics
* `metrics.total_nodes`: `6084`
* `metrics.total_ways`: `1032`
* `metrics.total_relations`: `38`
* `metrics.eligible_walkable_ways`: `440`
* `metrics.walkable_graph_vertices`: `1643`
* `metrics.walkable_way_segments`: `1793`
* `metrics.missing_node_references`: `0`
* `metrics.connected_components`: `24`
* `metrics.largest_component_vertices`: `1305`
* `metrics.mapped_entrances`: `10`
* `metrics.named_campus_objects`: `198`

## Beginner Walkthrough

1. In Java, `Properties` is a subclass of `Hashtable<Object, Object>` designed to read key-value pairs separated by `=` or `:` from text streams.
2. The `load(InputStream)` method parses the file into memory in `O(N)` time where $N$ is the number of configuration properties.
3. When the user opens the application, it can display "Data retrieved on 2026-10-22 from OpenStreetMap" in the status bar without hardcoding dates into Java source files.

## Viva Discussion Points

- *Why store metadata in a `.properties` file rather than hardcoding constants in Java?*  
  Decoupling data provenance from Java source code follows the Single Responsibility Principle. If the snapshot is refreshed in the future, only the data and properties files change, requiring zero Java code edits or recompilation.
- *What is ODbL and why is attribution required?*  
  OpenStreetMap data is licensed under ODbL 1.0. It requires that any public display of derived map data clearly credit "Map data © OpenStreetMap contributors" and link to its copyright terms.
