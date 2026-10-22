# `RouteResult.java` — File Guide

## Purpose

`RouteResult` encapsulates the complete outcome of a route calculation between two campus locations. It bundles the execution status enum, the ordered sequence of vertex IDs, the individual route legs, total distance in metres, and a user-facing explanation message.

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Algorithm Output:** Returned by `DijkstraShortestPath.findShortestPath()`.
- **UI Controller:** Consumed by JavaFX controllers to highlight paths on the canvas and populate the status text panel.

## Explicit Imports and Purpose

- `java.util.Collections`: Provides unmodifiable list wrappers.
- `java.util.List`: List interface for path vertices and legs.

## RouteStatus Enum

| Status Value | Meaning |
|---|---|
| `FOUND` | A valid connected walking route was found. |
| `SAME_LOCATION` | Start and destination are identical ($0.0\text{ m}$ distance). |
| `NO_PATH` | No path connects start and destination (disconnected components). |
| `INVALID_START` | Start location or node ID was not found in the graph. |
| `INVALID_DESTINATION` | Destination location or node ID was not found in the graph. |
| `UNCONNECTED_DESTINATION` | Destination POI is unconnected ($> 35.0\text{ m}$ from nearest path). |

## Key Methods

| Method | Role & Description |
|---|---|
| `isSuccess()` | Returns `true` if status is `FOUND` or `SAME_LOCATION`. |
| `getPathNodeIds()` | Ordered list of vertex IDs from start to finish. |
| `getLegs()` | List of individual `RouteLeg` segments. |
| `getTotalDistanceMeters()` | Total physical walking distance in metres. |
| `getMessage()` | User-friendly explanation string. |

## Viva Discussion Points

- *Why return an object instead of just a distance or list?*  
  It cleanly encapsulates success, empty paths, same-location edge cases, and distinct error conditions without relying on sentinel magic values (like $-1$ or `null`) or unchecked exceptions.

