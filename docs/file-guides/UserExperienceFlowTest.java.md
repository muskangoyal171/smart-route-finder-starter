# `UserExperienceFlowTest.java` — File Guide

## Purpose

`UserExperienceFlowTest` is an automated test suite located in `src/test/java/com/smartroute/ui/UserExperienceFlowTest.java`. It validates the end-to-end user experience flows, UI error handling states, input validations, leg formatting, and spanning tree exploration defined for SDLC Stage 7.

## Where It Is Used

- **Package:** `com.smartroute.ui`
- **Execution:** Run as part of the complete test suite via `AllTestsRunner` or individually using `java com.smartroute.ui.UserExperienceFlowTest`.
- **Validation Scope:** Validates that the application interface provides robust, user-friendly feedback without unhandled exceptions or invalid route states.

## Explicit Imports and Purpose

- `com.smartroute.algorithm.DijkstraShortestPath`: Single-source shortest path router.
- `com.smartroute.algorithm.GraphTraversal`: BFS traversal and component connectivity diagnostics.
- `com.smartroute.algorithm.MstResult`: Result container for spanning tree queries.
- `com.smartroute.algorithm.PrimMinimumSpanningTree`: Greedy minimum spanning tree algorithm.
- `com.smartroute.algorithm.RouteLeg`: Value object containing individual leg metrics and way names.
- `com.smartroute.algorithm.RouteResult`: Value object containing path status, distance, and explanation.
- `com.smartroute.algorithm.RouteResult.RouteStatus`: Execution status enum (`FOUND`, `SAME_LOCATION`, `NO_PATH`, `UNCONNECTED_DESTINATION`, etc.).
- `com.smartroute.data.CampusDataRepository`: High-level data accessor loading graph and destinations.
- `com.smartroute.model.CampusGraph`: Graph model holding 1,643 vertices and 1,793 segments.
- `com.smartroute.model.Location`: Selectable campus landmark model.
- `com.smartroute.testutil.SimpleAssert`: Zero-dependency test assertion utility.
- `java.util.HashSet`: Visited set for component membership checking.
- `java.util.List`: List interface.
- `java.util.Set`: Set interface.

## Test Scenarios Covered

1. **`testDestinationDropdownPopulation()`:**
   - Verifies that all 198 mapped campus landmarks are loaded and available for user selection.
   - Ensures no location has null or empty display labels.

2. **`testSearchLocationsFlow()`:**
   - Verifies partial-name and multi-word landmark search queries containing spaces (e.g. `"Gate"`, `"library"`, `"Main Gate"`, `"Food Court"`).
   - Validates that query results match expected campus locations.

2. **`testSuccessfulRouteCalculationAndLegFormatting()`:**
   - Validates end-to-end Dijkstra routing between connected campus destinations.
   - Verifies that the route yields positive distance, positive hop count, and non-empty legs.
   - Confirms that the mathematical sum of all individual route leg distances matches the reported total route distance within 0.01 metres.

3. **`testSameOriginAndDestinationSelfRoute()`:**
   - Tests selecting identical start and destination locations.
   - Confirms that the system returns success immediately with $0.0\text{ m}$ distance and 0 hops without executing unnecessary graph searches.

4. **`testUnconnectedLocationHandling()`:**
   - Tests route requests involving detached landmarks located $>35.0\text{ m}$ from the nearest walkable path.
   - Verifies that the router returns `UNCONNECTED_DESTINATION` and a clear, descriptive warning message.

5. **`testDisconnectedComponentsHandling()`:**
   - Tests route requests between vertices residing in mutually disjoint components of the campus graph.
   - Verifies that `NO_PATH` status is returned honestly with an explanatory message rather than infinite distance or fabricated lines.

6. **`testMinimumSpanningNetworkFlow()`:**
   - Tests Prim's minimum spanning network exploration.
   - Confirms that a valid `MstResult` is computed spanning all 1,442 vertices across 1,411 walkway edges with total length of $35,789.5\text{ m}$.

## Viva Discussion Points

- *Why must unconnected and disconnected destinations be tested explicitly?*  
  Real OpenStreetMap walking networks naturally contain disjoint clusters (e.g., isolated courtyards or detached perimeter walks) and POIs without nearby mapped paths. Ensuring the application reports `NO_PATH` and `UNCONNECTED_DESTINATION` honestly prevents misleading navigation instructions and crashes.

