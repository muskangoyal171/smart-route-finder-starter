# `RouteLeg.java` — File Guide

## Purpose

`RouteLeg` represents a single directed hop along a calculated route path between two consecutive OpenStreetMap vertices. It records the source and destination node IDs, the segment ground distance in metres, and the optional street or walkway name (e.g., "Fleming Road").

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Route Output:** Held inside `RouteResult.getLegs()`.
- **UI Display:** Used by JavaFX route details view to show step-by-step turn/segment distances and street names.

## Explicit Imports and Purpose

- `java.util.Objects`: Used for null-safe hash code and equality implementations.

## Key Methods and Invariants

| Method | Role & Description |
|---|---|
| `getStartNodeId()`, `getEndNodeId()` | Endpoint vertex IDs for this hop. |
| `getDistanceMeters()` | Segment distance in metres (constructor asserts non-negative). |
| `getWayName()` | Display name of the way or default `"Walkway"`. |

## Viva Discussion Points

- *Why break a route down into legs?*  
  Displaying individual legs allows users to inspect each path segment and distance, making route validation transparent rather than showing only a single opaque total distance.

