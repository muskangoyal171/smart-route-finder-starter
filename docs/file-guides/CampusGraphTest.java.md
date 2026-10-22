# `CampusGraphTest.java` — File Guide

## Purpose

`CampusGraphTest` is the automated unit test suite for `CampusGraph`. It validates the correctness of the spherical Earth Haversine formula against known ground coordinates, tests adjacency list construction, validates connected component counting using BFS, and verifies that graph invariant validations catch negative weights and dangling vertex references.

## Where It Is Used

- **Package:** `com.smartroute.model`
- **Execution:** Executed directly via `AllTestsRunner` or `CampusGraphTest.main()`, or via Maven Surefire test runner.

## Explicit Imports and Purpose

- `com.smartroute.testutil.SimpleAssert`: Test assertion framework.
- `java.util.List`: Checking edge collections.

## Test Cases Covered

1. `testHaversineDistanceKnownPoints()`:
   * Asserts distance between Main Gate (12.9692, 79.1559) and Silver Jubilee Tower (12.9716, 79.1594) falls within expected real-world range (~465m).
   * Asserts distance between identical coordinates is exactly 0.0m.
2. `testGraphConstructionAndNeighbors()`:
   * Builds a 3-node graph with 2 bidirectional edges.
   * Asserts vertex count (3), total directed edges (4), and correct neighbor relationships.
3. `testDisconnectedComponentCounting()`:
   * Constructs two separate disjoint components.
   * Asserts `countConnectedComponents()` returns 2.
4. `testNegativeDistanceRejection()`:
   * Validates that constructing a `Road` with negative weight throws `IllegalArgumentException`.
5. `testInvariantValidation()`:
   * Asserts that `validateInvariants()` detects an edge referencing an unindexed vertex ID and throws `IllegalStateException`.

## Viva Discussion Points

- *How do we know the Haversine formula is implemented correctly?*  
  By testing against known physical landmarks on the VIT Vellore campus (Main Gate to SJT) whose ground distance is known to be ~465 metres.

