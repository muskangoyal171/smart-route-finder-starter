# `MstResult.java` — File Guide

## Purpose

`MstResult` encapsulates the outcome of a Minimum Spanning Tree (MST) or Minimum Spanning Forest (MSF) calculation. It bundles the list of selected road edges, total network weight in metres, number of unique spanned vertices, algorithm name (`Prim` or `Kruskal`), and a boolean flag indicating whether the graph contained multiple disconnected components (forest).

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Output:** Returned by `PrimMinimumSpanningTree.findComponentMst()`, `PrimMinimumSpanningTree.findMinimumSpanningForest()`, and `KruskalMinimumSpanningTree.findMinimumSpanningTree()`.
- **UI Controller:** Consumed by JavaFX when displaying the Minimum Network overlay.

## Key Methods

| Method | Role & Description |
|---|---|
| `getAlgorithmName()` | Returns `"Prim"` or `"Kruskal"`. |
| `getEdges()` | Unmodifiable list of selected tree edges. |
| `getTotalWeightMeters()` | Sum of all tree edge weights in metres. |
| `getSpannedVertexCount()` | Number of unique vertices spanned. |
| `getEdgeCount()` | Number of edges in the spanning tree/forest. |
| `isForest()` | True if multiple disjoint components were connected into a forest. |

## Viva Discussion Points

- *Why distinguish between a tree and a forest?*  
  In a strictly connected graph, an MST has exactly $V - 1$ edges. In real-world crowdsourced OpenStreetMap data with disjoint components, the algorithm creates a Minimum Spanning Forest where each component $i$ has $V_i - 1$ edges.

