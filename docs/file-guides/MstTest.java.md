# `MstTest.java` — File Guide

## Purpose

`MstTest` is the automated unit test suite verifying the implementation of Minimum Spanning Tree (MST) algorithms, Disjoint Set Union (DSU), Prim and Kruskal equivalence, and BFS/DFS graph traversals.

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Execution:** Executed via `AllTestsRunner` or `MstTest.main()`.

## Test Scenarios Covered

1. **`testDisjointSetOperations`:**
   * Tests `makeSet`, `find` with path compression, `union` with union-by-rank.
   * Asserts component count decrementing and confirms that redundant unions return `false` to prevent cycles.
2. **`testPrimAndKruskalEquivalenceOnConnectedGraph`:**
   * Constructs a 5-node, 6-edge test graph.
   * Asserts that both Prim and Kruskal select exactly $V - 1 = 4$ edges and yield the exact same total weight ($75.0\text{ m}$).
3. **`testDisconnectedForestHandling`:**
   * Constructs two disconnected components.
   * Asserts that both Prim and Kruskal produce a spanning forest with $(3 - 1) + (2 - 1) = 3$ edges, total weight $45.0\text{ m}$, and flag `isForest() == true`.
4. **`testGraphTraversalBfsAndDfs`:**
   * Verifies BFS and DFS discovery orders, reachability checks (`isConnected`), and component discovery.
5. **`testMstOnRealCampusSnapshot`:**
   * Runs Prim and Kruskal over the real VIT Vellore campus snapshot `vit-vellore-campus.osm.xml`.
   * Asserts that total forest weight is strictly equal between Prim and Kruskal across all 31 components.

