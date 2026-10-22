# `Location.java` — File Guide

## Purpose

`Location` represents a selectable point of interest (POI) or destination on the VIT Vellore campus (e.g., *EV Periyar Central Library*, *Health Centre*, *Food Court*, *Main Gate*). It bridges high-level user destinations with low-level graph vertices.

## Where It Is Used

- **Package:** `com.smartroute.model`
- **Destination Registry:** Stored in `CampusGraph` and exposed to JavaFX combo boxes and search controls.
- **Snapping Policy:** Created by `OsmXmlCampusDataLoader` according to the 3-tier snapping strategy defined in `docs/osm-data-specification.md`.

## Explicit Imports and Purpose

- `java.util.Objects`: Used for null-safe hash code and equality implementations based on destination ID.

## Key Methods and Enums

### `Location.SnapType` Enum
- `DIRECT_VERTEX`: The location is already an explicit node on the walkable network.
- `MAPPED_ENTRANCE`: Connected via an entrance node (`node["entrance"]`) adjacent to the path.
- `PROXIMITY_SNAP`: Snapped to the closest graph vertex within the threshold ($\le 35.0\text{ m}$).
- `UNCONNECTED`: Too far from any walkable path ($> 35.0\text{ m}$); no route shortcut is fabricated.

### Key Methods
| Method | Role & Behavior |
|---|---|
| `getId()`, `getName()` | Destination identifier and display name. |
| `getConnectedNodeId()` | The graph vertex ID this destination connects to ($-1\text{L}$ if unconnected). |
| `isConnected()` | Returns `true` if connected to a valid path vertex; `false` otherwise. |
| `getSnapDistanceMeters()` | Distance in metres to the connected graph vertex ($0.0\text{ m}$ if direct). |

## DSA Concepts and Complexity

- **Destination Decoupling:** Separates routing graph topology from landmark names.
- **Honest Disconnection:** Prevents fictitious shortcuts across impassable obstacles or unmapped terrain.
- **Lookup Complexity:** $O(1)$ expected lookup via destination map.

## Viva Discussion Points

- *What happens when a destination is not connected to a path?*  
  Instead of guessing an artificial edge across buildings, the system marks the place as `UNCONNECTED`. The UI notifies the student honestly that no mapped path reaches this location.

