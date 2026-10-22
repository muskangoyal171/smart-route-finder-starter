# GitHub Copilot Repository Instructions

Before changing files, read `/plan.md`, the relevant portions of `/README.md` and `/AGENTS.md`, `docs/campus-data-workflow.md` for OSM/data work, and the matching file guide. Treat `plan.md` as the source of truth for stage status, approved decisions, verification, and next actions.

This is a Java 21 + Maven + JavaFX PAMCA501 DSA project. The student is a Java beginner: choose clear names, small methods, direct control flow, and teaching-oriented comments. Avoid streams-heavy code, unnecessary dependencies, and clever syntax that is harder to explain in a viva. Use explicit imports and a short purpose comment for each import. Explain each new class/method and non-obvious algorithm step.

## OpenStreetMap data rules

- OSM is the intended source for campus paths and destinations. The first release should use a local raw OSM XML snapshot exported manually from Overpass Turbo; do not call Overpass on startup or for individual route searches.
- Preserve OSM node IDs, coordinates, way tags, and the ordered way-node references. Build weighted graph edges between consecutive node references on eligible ways. Do not invent a direct path between nearby named destinations.
- Compute each edge's distance from geographic coordinates in metres (Haversine or another documented geodesic formula), not from Canvas coordinates or an arbitrary estimate.
- Do not assume all `highway=*` ways are walkable. Review the actual campus extract and document the approved allowlist plus `foot`, `access`, `barrier`, `construction`, and relevant restrictions. Exclude explicit `foot=no` and `access=no`; do not make unsupported claims about private access or accessibility.
- Prefer map-mapped entrances. For detached POIs, use a configurable, documented maximum snapping distance and leave destinations unconnected if the data does not support a safe connection.
- Show readable “Map data © OpenStreetMap contributors” attribution linked to `https://www.openstreetmap.org/copyright`. Document ODbL/source/retrieval-date details before distributing OSM data/derived graphs.
- The baseline is local vector rendering. Do not introduce remote tiles unless the plan reviews the provider's tile usage rules.
- Never fabricate the VIT campus bbox, building names, roads, paths, retrieval dates, build output, or verification results. If data is incomplete, document the gap.

## Architecture, DSA, testing, and documentation

Keep JavaFX controls/rendering separate from OSM parsing, the graph model, and algorithms. Use an adjacency list and Dijkstra for non-negative weighted routes; detect unreachable destinations. Prim/Kruskal are MST demonstrations, not point-to-point routing. BFS/DFS can help with graph connectivity. Add tests for algorithm changes, XML/tag parsing, missing references, distance calculations, access filters, disconnected networks, and destination snapping as those features are implemented.

For every new/changed source, test, configuration, Overpass query, or dataset file, create/update `docs/file-guides/<filename>.md` (preserve the filename extension, e.g. `OsmXmlCampusDataLoader.java.md`). Guides explain purpose, imports, methods, DSA concepts, complexity, tests, data provenance where applicable, and a beginner walkthrough.

At the end of each meaningful task, update the README and any affected Mermaid diagrams, then update `plan.md` with actual files changed, exact commands/scenarios and their real outcomes, open risks, and the next action. Never mark a stage complete unless every acceptance criterion has evidence. Never claim a build or test passed unless it actually ran.
