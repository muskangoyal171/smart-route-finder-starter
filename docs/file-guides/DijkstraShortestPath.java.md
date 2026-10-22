# `DijkstraShortestPath.java` — File Guide

## Purpose

`DijkstraShortestPath` is the standalone, decoupled implementation of Dijkstra's single-source shortest path algorithm on the `CampusGraph`. It extracts routing logic entirely away from UI drawing, accepts locations by name, `Location` object, or vertex ID, uses a binary min-heap priority queue, and reconstructs the optimal path sequence with exact metre weights.

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Application Routing:** Called when the user clicks "Find Shortest Route" in the UI.
- **Automated Testing:** Thoroughly tested by `DijkstraShortestPathTest`.

## Explicit Imports and Purpose

- `com.smartroute.algorithm.RouteResult.RouteStatus`: Result status enum.
- `com.smartroute.model.CampusGraph`: Graph model providing the adjacency list.
- `com.smartroute.model.Location`: Selectable destination model.
- `com.smartroute.model.Road`: Road edge segment model.
- `java.util.ArrayList`, `List`: Dynamic path and leg lists.
- `java.util.Collections`: List reversal utility (`Collections.reverse()`).
- `java.util.Comparator`: Comparing distances in the min-priority queue.
- `java.util.HashMap`, `Map`: Distance, parent vertex, and parent edge tables.
- `java.util.HashSet`, `Set`: Visited (settled) vertex set.
- `java.util.PriorityQueue`: Min-heap priority queue for optimal vertex selection.

## Algorithm Walkthrough

```mermaid
flowchart TD
    Init[Initialize dist map, parent map, visited set] --> Queue[Insert start vertex with distance 0.0]
    Queue --> Pop{PQ empty?}
    Pop -- Yes --> Unreachable[Check if destination reached; return NO_PATH if not]
    Pop -- No --> Poll[Poll vertex u with minimum tentative distance]
    Poll --> CheckTarget{Is u == destination?}
    CheckTarget -- Yes --> Reconstruct[Early Exit: Reconstruct path via parent pointers]
    CheckTarget -- No --> Visited{Is u in visited?}
    Visited -- Yes --> Pop
    Visited -- No --> Mark[Add u to visited]
    Mark --> Loop[For each edge u -> v in graph.getNeighbors(u)]
    Loop --> Relax{dist(u) + weight < dist(v)?}
    Relax -- Yes --> Update[Update dist(v), record parent, push v to PQ]
    Relax -- No --> Loop
    Update --> Loop
    Loop --> Pop
    Reconstruct --> Reverse[Reverse path and legs to source-to-destination order]
    Reverse --> Success[Return RouteResult.success]
```

1. **Validation:** Checks if start or end are identical (`SAME_LOCATION`), invalid, or unconnected before running Dijkstra.
2. **Min-Priority Queue:** Extracts vertex $u$ with lowest tentative distance in $O(\log V)$ time.
3. **Edge Relaxation:** Iterates over neighbors in the adjacency list `graph.getNeighbors(u)` in $O(\text{deg}(u))$ time; if `dist[u] + weight < dist[v]`, updates tentative distance and pushes into heap.
4. **Early Exit Optimization:** Stops the search immediately when the destination vertex is polled from the priority queue.
5. **Path Reconstruction:** Follows parent pointers backward from destination to source, then reverses both node and leg lists.

## Complexity Analysis

| Metric | Complexity | Explanation |
|---|---|---|
| **Time Complexity** | $O((V + E) \log V)$ | Each vertex is extracted from the min-heap at most once ($V \log V$), and each edge relaxation performs a heap insertion ($E \log V$). |
| **Space Complexity** | $O(V)$ | Distance map, parent maps, visited set, and priority queue each scale linearly with vertex count. |

## Viva Discussion Points

- *Why is Dijkstra optimal for this campus problem?*  
  All walking edge weights are strictly non-negative real ground distances ($w(e) \ge 0$). Under non-negative weights, Dijkstra is guaranteed to find the globally optimal shortest path without needing Bellman-Ford's slower $O(V \cdot E)$ approach.
- *How does the early exit optimization improve performance?*  
  By stopping as soon as the target vertex is finalized, the algorithm avoids exploring the remainder of the campus graph, significantly speeding up route calculation.

