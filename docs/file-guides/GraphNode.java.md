# `GraphNode.java` — File Guide

## Purpose

`GraphNode` represents a single point vertex in the OpenStreetMap (OSM) walking graph for the VIT Vellore campus. Each node maintains its permanent 64-bit OSM identifier, geographic coordinates (latitude and longitude), and optional key-value descriptive tags.

## Where It Is Used

- **Package:** `com.smartroute.model`
- **Graph Storage:** Held in `CampusGraph.nodes` map (`Map<Long, GraphNode>`).
- **Data Ingestion:** Instantiated by `OsmXmlCampusDataLoader` when parsing `<node>` XML tags.
- **Renderer / Routing:** Used to look up geographic coordinates for rendering vector segments on canvas and calculating edge weights.

## Explicit Imports and Purpose

- `java.util.Collections`: Provides unmodifiable views of node tags to preserve immutability.
- `java.util.HashMap`: Stores key-value OSM tags efficiently.
- `java.util.Map`: Standard interface for tag storage.
- `java.util.Objects`: Null-safe hashing and equality comparisons based on OSM node ID.

## Key Methods and Invariants

| Method | Role & Behavior |
|---|---|
| `getId()` | Returns the stable 64-bit OSM node ID. |
| `getLatitude()`, `getLongitude()` | Return geographic coordinates in decimal degrees. |
| `getTags()` | Returns an unmodifiable map of OSM tags. |
| `hasTag(String key)` | Checks if a tag key (e.g., `entrance`, `name`) exists. |
| `equals(Object)`, `hashCode()` | Identity based strictly on `id` (vertices are uniquely identified by OSM ID). |

## DSA Concepts and Complexity

- **Graph Vertex:** Acts as a vertex $v \in V$ in the graph $G = (V, E)$.
- **Hashing:** Implements `hashCode()` based on `id`, allowing $O(1)$ expected lookup time in `HashMap` and `HashSet`.
- **Space:** $O(1)$ fixed memory per node plus tag map size.

## Viva Discussion Points

- *Why use the OSM node ID instead of an auto-increment integer?*  
  OSM node IDs are globally stable and shared across ways. When two ways intersect in the real world, they reference the exact same OSM node ID, allowing intersections to connect naturally without fuzzy geometric snapping.

