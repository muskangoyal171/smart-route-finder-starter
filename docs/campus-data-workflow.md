# Campus data workflow — OpenStreetMap

This document explains how to prepare and refresh the local campus snapshot. The initial snapshot (`vit-vellore-campus.osm.xml`) and metadata (`campus-data-metadata.properties`) were exported and verified in Stage 2; follow this workflow whenever refreshing campus data in future maintenance stages.

## 1. Select the area

1. Open [Overpass Turbo](https://overpass-turbo.eu/).
2. Zoom and pan to include VIT Vellore's intended walking area and enough margin that connecting campus paths are not cut at the boundary.
3. Check that the selected area covers the destinations the first release is intended to support. Do not invent coordinates or assume every internal path is mapped.
4. Save the effective bounding box in `src/main/resources/data/campus-data-metadata.properties` after exporting.

## 2. Export the raw data

1. Copy `data/overpass/vit-vellore-campus.overpassql` into the Overpass Turbo query editor.
2. Run it for the chosen visible area. Inspect the result for obviously missing paths, overly large coverage, or incomplete geometry before accepting it.
3. Choose **Export → Data → raw data (OSM XML)**.
4. Save the file as `src/main/resources/data/vit-vellore-campus.osm.xml`.
5. Record the source URL, actual export/retrieval date, bounding box, query filename, attribution/licence, and known limitations in `campus-data-metadata.properties`.
6. Do not make the application call Overpass during normal startup or route calculation. Refresh the local snapshot deliberately and rerun all validation.

The query intentionally includes candidate `highway` ways broadly. A way tagged `highway=*` is not automatically suitable for the walking graph. The importer must use a reviewed allowlist plus relevant `foot`, `access`, `barrier`, `construction`, and related tags. This policy must be documented after looking at the actual campus extract.

## 3. Build a routing graph from the XML

OSM XML typically represents path geometry as `<node>` objects with IDs and coordinates and `<way>` objects containing an ordered sequence of `<nd ref="NODE_ID"/>` elements plus tags. Keep the IDs and order; they carry graph connectivity. Do not use only a place's label or the straight-line distance between two buildings to invent an edge.

For each allowed way:

1. Resolve consecutive node references to their latitude and longitude.
2. Create a candidate graph edge between each consecutive pair.
3. Calculate the segment distance with Haversine (metres) or another documented geodesic formula.
4. Preserve shared OSM node IDs so intersections connect. Do not connect crossing lines without a shared node unless a separate, explicit, tested rule is approved.
5. Report unresolved node references and invalid coordinates; do not invent replacements.

## 4. Add selectable destinations

- Prefer an entrance node mapped onto a valid path/road graph.
- A named node that is already part of the graph may be directly selectable.
- A named building/POI that is separate from the graph may be attached to the nearest graph node only within a documented, configurable maximum distance.
- If there is no suitable graph node within the threshold, mark the destination as not connected. Do not silently draw a shortcut across a building, wall, or private area.
- Keep `name` tags, OSM object IDs, and the method used to attach each destination so a result can be debugged.

## 5. Review data quality

For each new snapshot, record:

- How many OSM nodes/ways were read and how many candidate paths passed the filter.
- Invalid or missing node references and invalid/zero-length segments skipped.
- Number of connected graph components and selectable destinations not connected to any component.
- Whether the important entries/exits, academic blocks, library, hostels, food areas, sports areas, admin buildings, auditorium, and medical facilities actually appear in OSM. This is a review checklist, not permission to invent missing entries.
- A handful of routes spot-checked against the OSM view and, where available, campus-verified access information.
- Any uncertain, closed, restricted, indoor, stair-only, or accessibility-related path data.

An OSM snapshot can be incomplete or stale. Describe routes as the shortest route through the loaded mapped graph, not as an official or guaranteed route on campus.

## 6. Attribution and public distribution

Show readable map attribution, for example **“Map data © OpenStreetMap contributors”**, near the rendered map and link it to [https://www.openstreetmap.org/copyright](https://www.openstreetmap.org/copyright) where supported. Document the ODbL in the README and retain data provenance. Before sharing raw OSM data or a graph derived from it, review current [OpenStreetMap Foundation attribution guidelines](https://osmfoundation.org/wiki/Attribution_Guidelines) and the licence guidance for the specific use.

The app's initial renderer uses local vector geometry and does not need map tiles. If tiles are considered later, review the relevant tile provider usage policy separately; permission to use OSM data does not automatically mean an arbitrary tile service can be used in any way.

## 7. Refresh procedure

1. Repeat the export deliberately for the same reviewed area, and note any bounding-box change.
2. Compare the new extract's size/coverage and inspect important paths before replacing the current snapshot.
3. Update metadata and preserve the query used.
4. Run parser/data tests, graph-connectivity checks, and the route spot-check list.
5. Update any file guide for changed data files, the README if coverage/capabilities changed, and `plan.md` with the actual verification and known issues.
6. Only then use the new snapshot as the current project dataset.

## References

- [Overpass Turbo](https://overpass-turbo.eu/)
- [Overpass Turbo on OpenStreetMap Wiki](https://wiki.openstreetmap.org/wiki/Overpass-Turbo)
- [OpenStreetMap copyright/licence page](https://www.openstreetmap.org/copyright)
- [OSM Foundation attribution guidelines](https://osmfoundation.org/wiki/Attribution_Guidelines)
