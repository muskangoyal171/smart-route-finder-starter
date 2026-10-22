# `CampusGraph.java` — File Guide

## Purpose

`CampusGraph` is the core graph data structure model for the VIT Vellore walking network. It encapsulates the weighted adjacency list, vertex index, destination registry, geodesic Haversine distance calculations, landmark search queries, invariant validation, and connectivity diagnostics.

## Where It Is Used

- **Package:** `com.smartroute.model`
- **Data Repository:** Built by `OsmXmlCampusDataLoader` and stored in `CampusDataRepository`.
- **Routing Algorithms:** Passed to Dijkstra, Prim, and Kruskal algorithms to traverse vertices and neighbors.
- **GUI Controller:** Used by `SmartRouteFinderGUI` for landmark lookup, real-time typing search, and validation.

## Explicit Imports and Purpose

- `java.util.ArrayDeque`: Efficient standard queue for BFS graph traversal.
- `java.util.ArrayList`: Dynamic list backing outgoing edges in the adjacency list and search results.
- `java.util.Collections`: Unmodifiable collection views preserving graph immutability.
- `java.util.HashMap`: Fast average $O(1)$ vertex and neighbor lookup.
- `java.util.HashSet`: Fast visited set tracking during BFS traversals.
- `java.util.LinkedHashMap`: Preserves predictable insertion order of selectable destinations.
- `java.util.List`, `java.util.Map`, `java.util.Queue`, `java.util.Set`: Core Java Collections Framework interfaces.

## Key Methods and Invariants

| Method | Role & Description |
|---|---|
| `calculateHaversineDistance(lat1, lon1, lat2, lon2)` | Static utility calculating geodesic distance in metres on a spherical Earth ($R = 6,371,000\text{ m}$). |
| `addNode(GraphNode)` | Inserts a vertex and initializes its empty edge list. |
| `addBidirectionalRoad(Road)` | Inserts both forward ($u \to v$) and reverse ($v \to u$) directed edges. |
| `getNeighbors(long nodeId)` | Returns unmodifiable list of outgoing `Road` edges for vertex `nodeId` in $O(1)$ time. |
| `searchLocationsByName(String query)` | Searches registered campus landmarks whose names contain `query` (case-insensitive); returns unmodifiable list. |
| `countConnectedComponents()` | Executes BFS across all vertices to count disjoint connected components. |
| `validateInvariants()` | Verifies that all edges reference existing vertices and weights are strictly non-negative. |

## DSA Concepts and Complexity Analysis

- **Graph Model:** Weighted undirected graph $G = (V, E)$ represented via an adjacency list: `Map<Long, List<Road>>`.
- **Space Complexity:** $O(V + E)$ where $V = 1,643$ vertices and $E = 1,793$ undirected edges (3,586 directed edges).
- **Neighbor Retrieval:** $O(1)$ average time to retrieve incident edges for a node.
- **Component Traversal:** $O(V + E)$ using standard queue-based Breadth-First Search (BFS).
- **Search Complexity:** $O(D)$ where $D = 198$ destinations; string containment scanning across all registered landmarks.

## Viva Discussion Points

- *Why choose an adjacency list over an adjacency matrix?*  
  Campus walking graphs are sparse ($E \ll V^2$). For 1,643 vertices, an adjacency matrix would require $(1643)^2 \approx 2.7 \times 10^6$ entries (mostly empty zeros), wasting memory and making neighbor iteration $O(V)$. The adjacency list uses only $O(V + E)$ memory and allows $O(\text{deg}(v))$ neighbor traversal, optimal for Dijkstra.
- *How does search work without external database indexing?*  
  With 198 landmarks, an in-memory linear scan of strings is computationally negligible (< 0.1 ms on any modern CPU) and avoids external database dependencies or heavyweight indexing libraries.
