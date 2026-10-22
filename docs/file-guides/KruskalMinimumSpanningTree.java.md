# `KruskalMinimumSpanningTree.java` — File Guide

## Purpose

`KruskalMinimumSpanningTree` implements Kruskal's edge-centric greedy algorithm to construct a Minimum Spanning Tree (or Forest) on the `CampusGraph`. It sorts all unique undirected edges by weight and uses `DisjointSet` (Union-Find) to detect and avoid cycles.

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Course Demonstration:** Provides the PAMCA501 syllabus demonstration of Kruskal's algorithm, edge sorting, and Disjoint Set Union.
- **Unit Testing:** Verified in `MstTest`.

## Explicit Imports and Purpose

- `com.smartroute.model.CampusGraph`: Graph model providing edges and vertices.
- `com.smartroute.model.Road`: Road edge segment model.
- `java.util.ArrayList`, `List`: Edge collections.
- `java.util.Comparator`: Comparing edge weights for sorting.
- `java.util.HashSet`, `Set`: Deduplicating bidirectional edges and tracking spanned vertices.

## Algorithm Flow

```mermaid
flowchart TD
    Start[Extract all unique undirected edges] --> Sort[Sort edges by weight in ascending order: O(E log E)]
    Sort --> InitDSU[Initialize DisjointSet: makeSet for all V vertices]
    InitDSU --> Loop{More edges in sorted list?}
    Loop -- No --> Done[Return MstResult]
    Loop -- Yes --> Edge[Take next edge u -> v with weight w]
    Edge --> CheckCycle{dsu.union(u, v) == true?}
    CheckCycle -- Yes --> Add[Add edge to selectedEdges; add w to totalWeight]
    CheckCycle -- No --> Discard[Discard edge: would form a cycle]
    Add --> Loop
    Discard --> Loop
```

## Complexity Analysis

| Metric | Complexity | Explanation |
|---|---|---|
| **Time Complexity** | $O(E \log E)$ | Sorting $E$ edges takes $O(E \log E)$. The $E$ union-find operations take $O(E \cdot \alpha(V)) \approx O(E)$, so sorting dominates. |
| **Space Complexity** | $O(V + E)$ | Storage for unique edges and DisjointSet structures. |

## Viva Discussion Points

- *When is Kruskal preferred over Prim?*  
  Kruskal is naturally suited for sparse graphs ($E \ll V^2$) and when edges are already sorted or easily sorted. Prim is often preferred on dense graphs ($E \approx V^2$) when using an adjacency matrix or Fibonacci heap.
- *Does Kruskal always produce the same total weight as Prim?*  
  Yes. Even though the specific chosen edges might differ when tie weights exist, the **total minimum spanning tree weight** is mathematically guaranteed to be identical for both algorithms.

