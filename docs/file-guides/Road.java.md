# `Road.java` — File Guide

## Purpose

`Road` represents a single directed edge in the campus graph between two consecutive OSM node vertices. It records the source and target node IDs, the physical ground distance in metres (calculated via Haversine formula), the parent way ID, the highway classification tag (e.g., `footway`, `service`), and the optional road name.

## Where It Is Used

- **Package:** `com.smartroute.model`
- **Adjacency List:** Elements of the edge lists in `CampusGraph.adjacencyList` (`Map<Long, List<Road>>`).
- **Data Ingestion:** Instantiated by `OsmXmlCampusDataLoader` for each consecutive `<nd ref="..."/>` pair on eligible ways.
- **Routing:** Evaluated by Dijkstra and MST algorithms to determine least-cost path traversal.

## Explicit Imports and Purpose

- `java.util.Objects`: Used for null-safe hash code and equality implementations.

## Key Methods and Invariants

| Method | Role & Behavior |
|---|---|
| `getStartNodeId()`, `getEndNodeId()` | Source ($u$) and destination ($v$) vertex IDs. |
| `getDistanceMeters()` | Segment weight in metres. Constructor validates that distance $\ge 0.0\text{ m}$. |
| `getWayId()`, `getHighwayType()` | Provenance metadata indicating the source OSM way and classification. |
| `createReverse()` | Creates a reversed edge ($v \to u$) with identical weight and attributes for undirected traversal. |

## DSA Concepts and Complexity

- **Weighted Directed Edge:** Represents a directed arc $e = (u, v)$ with non-negative weight $w(e) \ge 0$.
- **Adjacency Modeling:** In undirected campus walkways, bidirectional paths are modeled as two opposite directed `Road` edges.
- **Complexity:** $O(1)$ construction and reverse creation time.

## Viva Discussion Points

- *Why are edge weights in metres instead of pixel coordinates?*  
  Screen canvas dimensions change when zooming or resizing the window. Computing geodesic distance from geographic coordinates using the Haversine formula ensures edge weights represent real physical walking distance.

