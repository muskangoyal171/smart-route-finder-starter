# `GraphTraversal.java` — File Guide

## Purpose

`GraphTraversal` provides diagnostic graph traversal algorithms for the `CampusGraph`, implementing standard Breadth-First Search (BFS) and iterative Depth-First Search (DFS) for component discovery, path reachability, and traversal order inspection.

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Diagnostics:** Inspects reachability between locations before initiating route calculations.
- **Unit Testing:** Verified in `MstTest.testGraphTraversalBfsAndDfs()`.

## Explicit Imports and Purpose

- `com.smartroute.model.CampusGraph`: Graph model providing nodes and adjacency list.
- `com.smartroute.model.Road`: Road edge segment model.
- `java.util.ArrayDeque`: Standard FIFO Queue for BFS and LIFO Stack for iterative DFS.
- `java.util.ArrayList`, `List`: Dynamic traversal order collections.
- `java.util.Collections`: Sorting and unmodifiable utilities.
- `java.util.HashSet`, `Set`: Visited vertex tracking set.
- `java.util.Queue`: Queue interface.

## Key Methods

| Method | Role & Description |
|---|---|
| `bfs(long startNodeId)` | Explores graph level by level using an `ArrayDeque` FIFO queue; returns discovery order. |
| `dfs(long startNodeId)` | Explores graph branch by branch using an `ArrayDeque` LIFO stack; returns discovery order. |
| `isConnected(long startNodeId, long targetNodeId)` | Returns `true` if `targetNodeId` is reachable from `startNodeId`; `false` otherwise. |
| `findComponentSizes()` | Discovers all connected components and returns a descending list of their vertex counts. |

## Complexity Analysis

- **Time Complexity:** $O(V + E)$ visiting each reachable vertex and edge once.
- **Space Complexity:** $O(V)$ auxiliary space for visited sets, queue, and stack storage.

## Viva Discussion Points

- *Why use an explicit stack for DFS instead of recursion?*  
  Large graphs with long chains can cause deep recursion, leading to `java.lang.StackOverflowError`. Using an explicit `ArrayDeque` on the heap prevents stack overflow regardless of graph depth.

