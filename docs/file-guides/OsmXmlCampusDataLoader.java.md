# `OsmXmlCampusDataLoader.java` — File Guide

## Purpose

`OsmXmlCampusDataLoader` is the secure OpenStreetMap XML parser. It parses raw OSM XML snapshots (such as `vit-vellore-campus.osm.xml`), applies the walkable-way allowlist, rejects prohibited access paths, computes geodesic segment weights using the Haversine formula, snaps destinations to entrance nodes or adjacent vertices with main-component priority, disambiguates duplicate landmark names, and returns a fully populated `CampusGraph`.

## Where It Is Used

- **Package:** `com.smartroute.data`
- **Data Repository:** Executed by `CampusDataRepository` during startup to load campus geometry completely offline.
- **Specification:** Directly implements the ingestion rules specified in `docs/osm-data-specification.md`.

## Explicit Imports and Purpose

- `com.smartroute.algorithm.GraphTraversal`: Used for BFS traversal to identify the main campus connected component for priority snapping.
- `com.smartroute.model.CampusGraph`: Graph model holding vertices, edges, and destination POIs.
- `com.smartroute.model.GraphNode`: Graph vertex with geographic coordinates.
- `com.smartroute.model.Location`: Selectable campus landmark with display name and snap status.
- `com.smartroute.model.Location.SnapType`: Enum specifying entrance snap, proximity snap, or unconnected.
- `com.smartroute.model.Road`: Physical walkway edge model with highway type and real distance.
- `java.io.File`, `java.io.FileInputStream`, `java.io.InputStream`, `java.io.IOException`: Standard I/O operations.
- `java.util.*`: Java collections for node dictionaries, way lists, and component sets.
- `javax.xml.parsers.DocumentBuilder`, `DocumentBuilderFactory`: Standard DOM XML parser.
- `org.w3c.dom.*`: Element, Document, NodeList.
- `org.xml.sax.SAXException`: XML parsing errors.

## Security Configuration

To guard against XML External Entity (XXE) vulnerabilities, external entity parsing and external DTDs are explicitly disabled:
```java
dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
```

## Key Ingestion Steps

1. **Parse Nodes:** Extract all `<node id="..." lat="..." lon="...">` elements and their tags.
2. **Filter Ways:** Ingest ways matching `footway, path, pedestrian, steps, service, residential, living_street, crossing, unclassified, track`. Reject `foot=no`, `access=no`, `highway=construction`. Including `crossing` connects 37 pedestrian crosswalks across campus roads.
3. **Construct Edges:** Connect consecutive node references along allowed ways; calculate Haversine distance in metres. Skip zero-length segments ($d < 0.1\text{ m}$).
4. **Identify Main Component:** Run BFS component analysis to identify the set of node IDs belonging to the largest campus component.
5. **Main Component Priority Snapping:**
   - First, search for nearest walkway node in the **main campus component** within `50.0m`.
   - If not found within 50m of main network, fall back to nearest node across any component within `50.0m`.
   - If nearest node is $> 50.0\text{ m}$, designate landmark as `UNCONNECTED`.
   - This ensures landmarks near building entrances attach to the navigable main campus network rather than isolated two-vertex service path fragments.
6. **Disambiguate Duplicate Names:** Detects multiple locations sharing the same name (e.g. multiple "Nescafe" or "Indian Bank" kiosks) and appends distinct indices (e.g., "Nescafe (1)", "Nescafe (2)") so every destination is uniquely selectable.
7. **Validate Invariants:** Verify no orphaned edge references or negative weights before returning graph.

## Viva Discussion Points

- *Why include `highway=crossing` in the walkable allowlist?*  
  Campus roads are traversed by marked pedestrian crosswalks. Without `crossing`, pedestrian paths on opposite sides of campus avenues are disconnected, splitting the walkable graph into isolated fragments.
- *Why prioritize snapping to the main connected component?*  
  A POI may be geographically 10m from an isolated service path behind a wall, but 12m from the main campus walkway. Blind Euclidean snapping would attach it to the dead-end fragment, causing route searches to the rest of the campus to fail. Prioritizing the main component ensures reliable end-to-end pathfinding.
