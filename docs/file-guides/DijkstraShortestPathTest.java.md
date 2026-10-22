# `DijkstraShortestPathTest.java` — File Guide

## Purpose

`DijkstraShortestPathTest` is the automated unit test suite verifying the correctness and robustness of `DijkstraShortestPath`. It covers optimality, direct vs. indirect routes, edge cases, disconnected components, and real campus snapshot routing.

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Execution:** Executed via `AllTestsRunner` or `DijkstraShortestPathTest.main()`.

## Test Scenarios Covered

1. **`testDirectVsIndirectShorterPath`:**
   * Graph: Direct path $A \to C = 100\text{ m}$; indirect path $A \to B = 30\text{ m}, B \to C = 40\text{ m}$.
   * Verifies that Dijkstra chooses the indirect route $A \to B \to C$ with distance $70.0\text{ m}$, verifying edge relaxation.
2. **`testSameStartAndDestination`:**
   * Verifies that routing from $A \to A$ returns $0.0\text{ m}$, status `SAME_LOCATION`, and a 1-node path.
3. **`testUnreachableDestination`:**
   * Verifies that searching between vertices in disconnected components returns `NO_PATH` with infinite distance without crashing.
4. **`testInvalidEndpoints`:**
   * Verifies graceful error statuses (`INVALID_START`, `INVALID_DESTINATION`) when querying unindexed node IDs.
5. **`testUnconnectedLocationHandling`:**
   * Verifies that destinations marked `UNCONNECTED` return `UNCONNECTED_DESTINATION`.
6. **`testRealCampusGraphRoute`:**
   * Loads the real 1.50 MB snapshot (`vit-vellore-campus.osm.xml`).
   * Routes between connected locations and asserts that the sum of leg distances exactly equals total path distance.

## Viva Discussion Points

- *What test proves that Dijkstra doesn't just pick the single edge with the fewest hops?*  
  `testDirectVsIndirectShorterPath` specifically pits a 1-hop 100m path against a 2-hop 70m path. Dijkstra correctly chooses the 2-hop path because its total weight is lower.

