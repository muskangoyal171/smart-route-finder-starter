# `vit-vellore-campus.overpassql` — File Guide

## Purpose

This file contains the Overpass Turbo query for extracting raw OpenStreetMap (OSM) XML data for the VIT Vellore campus (Katpadi, Vellore, Tamil Nadu, India). It targets candidate pedestrian and access highways, named campus landmarks, building outlines, and entrance nodes within the reviewed campus bounding box.

The extracted XML snapshot serves as the local offline data source for the Java desktop application, avoiding runtime network dependencies on Overpass.

## Where it is used

- **Manual Export Tool:** Run directly in [Overpass Turbo](https://overpass-turbo.eu/).
- **Canonical Output File:** Exported as raw XML to `src/main/resources/data/vit-vellore-campus.osm.xml` in Stage 2.
- **Related Documentation:**
  - [`docs/osm-data-specification.md`](../osm-data-specification.md): Authoritative specification for bounding box, filters, and snapping rules.
  - [`docs/campus-data-workflow.md`](../campus-data-workflow.md): Step-by-step campus data preparation and refresh guide.
  - [`plan.md`](../../plan.md): Project control and SDLC stage tracking.

## Query Structure and Parameters

```overpassql
[out:xml][timeout:90];
(
  way["highway"](12.9640, 79.1500, 12.9790, 79.1670);
  node["name"](12.9640, 79.1500, 12.9790, 79.1670);
  node["entrance"](12.9640, 79.1500, 12.9790, 79.1670);
  way["name"](12.9640, 79.1500, 12.9790, 79.1670);
  way["building"](12.9640, 79.1500, 12.9790, 79.1670);
  relation["building"](12.9640, 79.1500, 12.9790, 79.1670);
  relation["amenity"="university"](12.9640, 79.1500, 12.9790, 79.1670);
);
(._;>;);
out body;
```

* **Bounding Box `(12.9640, 79.1500, 12.9790, 79.1670)`:**
  * South: `12.9640` (Tiruvalam Road and Main Gate southern approach).
  * West: `79.1500` (Katpadi railway line buffer and western perimeter).
  * North: `12.9790` (Northern boundary including all Men's Hostels and outdoor sports grounds).
  * East: `79.1670` (Silver Jubilee Tower and Ladies Hostels perimeter).
* **`way["highway"]`**: Captures footways, paths, pedestrian areas, steps, and campus service roads.
* **`node["entrance"]`**: Captures building entrances that provide clean connection points to adjacent footways.
* **`node["name"]` / `way["name"]`**: Captures named academic blocks, hostels, and campus landmarks for destination selection.
* **`relation["building"]` / `relation["amenity"="university"]`**: Captures campus-wide administrative relations and complex multipolygon footprints.
* **`(._;>;); out body;`**: Recursively includes all constituent child nodes (`<nd ref="..."/>`) with coordinates, essential for path graph reconstruction.

## Limitations and Ingestion Rules

1. **Local Filtering Required:** The query retrieves candidate highways broadly. The Java parser must strictly filter them using the allowlist (`footway`, `path`, `pedestrian`, `steps`, `service`, `residential`, `living_street`) and exclude `foot=no` and `access=no`.
2. **Offline Baseline:** This query is executed manually for version-controlled snapshots; the application never executes queries against Overpass during normal startup or routing.
3. **Crowdsource Constraints:** Mapped ways reflect community mapping on the retrieval date; unmapped interior corridors or newly constructed paths will not appear unless updated in OSM and re-exported.

## Beginner Walkthrough

1. An Overpass query acts like a database query for map data.
2. The coordinate box restricts the search strictly to the VIT Vellore area so irrelevant external roads in Vellore city are excluded.
3. The recursive step `(._;>;);` is crucial: without it, ways would only list node references by numeric ID without including the node tags where actual latitude and longitude reside.
4. The downloaded XML will contain `<node id="..." lat="..." lon="...">` and `<way id="..."><nd ref="..."/></way>` tags that our Java importer reads to build the graph.

## Viva Discussion Points

- *Why use raw OSM XML instead of GeoJSON?*  
  OSM XML preserves the exact topological connectivity: ways reference ordered node IDs, allowing shared intersection nodes to be directly detected without fuzzy coordinate matching.
- *Why is a bounding box needed?*  
  Without a bounded query, fetching university data across the entire state or district would overload the Overpass server and introduce external non-campus nodes into our local graph.
