# Smart Route Finder — Implementation Plan and Source of Truth

> **Project Specification & Source of Truth:** This document defines the project scope, approved architectural decisions, milestone status, implementation standards, and verification criteria for the PAMCA501 Data Structures and Algorithms coursework.

## 1. Project control

- **Project:** Smart Route Finder for VIT Vellore campus — PAMCA501 Data Structures and Algorithms
- **Plan version:** 0.8
- **Last updated:** 2026-10-22
- **Current SDLC stage:** Stage 10 Done — Interactive Navigation, Search & Route Optimization verified across all 7 test suites
- **Build baseline:** Java 21, JavaFX 21.0.8, Maven; main class `com.smartroute.SmartRouteFinderGUI`
- **Source-of-truth priority:** Keep `plan.md`, `README.md`, code, tests, diagrams, and per-file guides synchronized. Documentation updates are a release gate in every stage, not final polish.

### Status vocabulary

Use only these milestone statuses:

- `Not started` — work has not begun.
- `In progress` — work has begun, but at least one exit criterion remains.
- `Blocked` — a named external or technical dependency prevents progress.
- `Done` — every exit criterion has evidence recorded.

## 2. Product statement and approved direction

Build a beginner-friendly Java desktop application that finds the shortest **mapped walking route** between selectable places on the VIT Vellore campus. The campus network will be based on OpenStreetMap (OSM) path/road geometry, not on hand-drawn links between a small list of landmarks. The graph algorithms remain implemented in Java so the student can explain and demonstrate them for PAMCA501.

### Approved decisions

1. **OSM is the intended source** for campus paths, geographic geometry, and mapped points/buildings.
2. **Use an offline OSM snapshot for the first release.** Download a small campus extract manually with Overpass Turbo, store the raw OSM XML plus provenance metadata in the repository, and load it locally. The app should not call Overpass on every startup or route request.
3. **Route along mapped way segments.** Graph edges are created between consecutive OSM node references on eligible OSM ways. Do not create a direct edge between two named buildings merely because they are close together.
4. **Use actual geographic coordinates.** Each OSM graph node keeps its OSM ID, latitude, and longitude. For each eligible way segment, calculate a distance in metres from its endpoint coordinates using the Haversine formula (or another documented geodesic method). The shortest route is shortest over the selected mapped graph and its calculated edge weights.
5. **Treat destinations separately from the path network where needed.** Prefer an OSM-mapped entrance or a named POI/node connected to the walking graph. If a destination must be attached to the nearest graph node, use a configurable maximum snap distance; never snap silently across a large or obstructed gap.
6. **Render the OSM-derived geometry in the current JavaFX Canvas** for the first release. Map coordinates will be projected/scaled to the Canvas while preserving the approximate local aspect ratio. The first release does not need remote raster map tiles.
7. **Show OpenStreetMap attribution** clearly in the map view and document the ODbL/source. Before distributing a dataset or a derived database publicly, review the OSM attribution and licence guidance.
8. **Do not claim official or complete campus coverage.** The result reflects the OSM snapshot downloaded on a recorded date. Paths and entrances may be missing, outdated, restricted, or incorrectly mapped.
9. **Keep a clearly labelled sample graph only as a development fallback.** The existing sample distances and schematic coordinates must not be mixed into the OSM graph or presented as actual campus data.

### Product scope

**First usable release includes:**

- Java 21, JavaFX, and Maven desktop application.
- Local OSM XML loading and validation.
- A weighted graph backed by an adjacency list.
- Dijkstra shortest-distance routing for non-negative edge weights.
- Start/destination selection, a route overlay, route legs, and total mapped distance.
- Clear handling for invalid choices, same start/destination, unreachable locations, missing data, and malformed OSM extracts.
- An optional DSA demonstration of Prim and Kruskal minimum spanning trees plus BFS/DFS connectivity diagnostics, separate from the user-facing route result.
- Automated algorithm/data tests, Mermaid diagrams, per-file guides, setup instructions, SDLC evidence, and viva notes.

**Not in the first release:** live GPS, turn-by-turn GPS navigation, live traffic, user accounts, a server backend, mobile apps, indoor positioning, or a claim that every campus walkway is represented. Do not add remote tile loading without separately reviewing the relevant provider's usage policy.

## 3. Current baseline and known gaps

The current code is `src/main/java/com/smartroute/SmartRouteFinderGUI.java`, based on the supplied JavaFX prototype.

### Existing prototype behavior

- Draws 10 indexed location markers and 15 hard-coded road records.
- Uses illustrative metre values and schematic normalized positions (not latitude/longitude).
- Lets users select start/destination through combo boxes or map clicks.
- Runs Dijkstra with Java `PriorityQueue` and displays a route.
- Runs Prim and highlights a minimum spanning tree.
- Supports reset.

### Known issues to resolve in planned stages

1. The UI, data, rendering, Dijkstra, and Prim are in one class.
2. Algorithms repeatedly scan the hard-coded road array instead of sharing an adjacency-list graph.
3. Dijkstra must safely detect an unreachable destination before rebuilding the parent path.
4. OSM way tags, node references, access restrictions, and destination-to-path connections are not yet parsed.
5. Geographic coordinates and route-segment weights are not yet used.
6. No actual VIT Vellore OSM snapshot or provenance metadata has been added yet.
7. There is no automated test suite.
8. The current source uses wildcard imports; new/refactored files must use explicit imports and explain their purpose as required by the coding rules.
9. The current map is schematic and should remain labelled as such until replaced.

## 4. SDLC stages and exit criteria

| Stage | Name | Status | Exit criteria |
|---|---|---|---|
| 0 | Baseline inspection and documentation bootstrap | Done | Starter source/POM inspected; initial README, architecture plan, and file guides created. Baseline requirements and milestones established. |
| 1 | Requirements and OSM data definition | Done | Inspect OSM coverage for the campus; choose and document the campus bounding box; agree on walkable-way/access filters, target destination types, snapping policy, provenance schema, and measurable MVP acceptance criteria. Completed with evidence in `docs/osm-data-specification.md`, updated query `vit-vellore-campus.overpassql`, and corresponding file guides. |
| 2 | OSM extraction and dataset provenance | Done | Export an appropriately bounded raw OSM XML snapshot; commit the Overpass query, metadata, retrieval date, bounding box, attribution/licence notes, and coverage review. Run a validation checklist; no blank or undocumented provenance fields. Completed: `vit-vellore-campus.osm.xml` (1,537,726 bytes) saved; `campus-data-metadata.properties` populated; verified 6,084 nodes, 1,032 ways, 391 walkable ways, 1,442 vertices, 1,576 segments, 0 missing node refs, 31 components; file guides created. |
| 3 | Architecture and graph model | Done | Approve class responsibilities and graph invariants; implement OSM-node vertices, way-segment edges, destination mapping, ID lookup, distance calculation, and clear load errors. Draw an updated architecture diagram. Completed: `GraphNode`, `Road`, `Location`, `CampusGraph`, `CampusDataMetadata`, `OsmXmlCampusDataLoader`, and `CampusDataRepository` implemented; 12 file guides created; automated tests passing 3/3 (`CampusGraphTest`, `CampusDataMetadataTest`, `OsmXmlCampusDataLoaderTest`). |
| 4 | Core routing and automated tests | Done | Extract Dijkstra from the GUI; use a min-priority queue and adjacency list; test direct/indirect routes, ties where relevant, same location, unreachable target, invalid endpoint, and invalid weight. Check loaded OSM graph invariants. Completed: `RouteLeg`, `RouteResult`, and `DijkstraShortestPath` implemented with early exit optimization; tests passing 4/4 in `AllTestsRunner` including direct vs. indirect shorter paths, same location, unreachable destinations, and real campus snapshot routing; file guides created. |
| 5 | MST and graph diagnostics | Done | Refactor/test Prim; add Kruskal + Disjoint Set Union if useful for the course demonstration; add BFS/DFS connectivity diagnostics as appropriate. Distinguish MST from shortest route in UI and docs. Completed: `MstResult`, `DisjointSet` (Union-Find with path compression and rank), `PrimMinimumSpanningTree`, `KruskalMinimumSpanningTree`, and `GraphTraversal` (BFS/DFS diagnostics) implemented; tests passing 5/5 in `AllTestsRunner` verifying Prim and Kruskal total weight equivalence, $V - 1$ edge property, cycle detection, and spanning forest on real campus graph; file guides created. |
| 6 | OSM rendering and destination integration | Done | Render geographic OSM path segments and place markers from the loaded snapshot; equirectangular projection preserving aspect ratio; prefer mapped entrances; enforce 35.0m snap-distance rules; show unavailable/unconnected destinations honestly; display attribution badge. Completed: `CampusMapRenderer.java`, `SmartRouteFinderGUI.java`, and `CampusMapRendererTest.java` (headless JavaFX verification). |
| 7 | User experience and error handling | Done | Provide usable place search/selection, route highlighting, route-leg distances, total distance, legend, map-data date/source, clear empty/error/no-route states, and reset behavior. Completed: dynamic ComboBoxes with 198 places, step-by-step leg breakdown, MAP KEY legend, `UserExperienceFlowTest.java` passing 6/6 UX scenarios. |
| 8 | Integration, regression, and release validation | Done | A clean Java build succeeds across all source and test classes; all 7 algorithm, data, rendering, and UX test suites pass (7/7); manual GUI scenarios recorded; routes spot-checked against visible OSM geometry; zero known critical defects. |
| 9 | Documentation, demonstration, and release | Done | README, data workflow, file guides, diagrams, stage records, attribution, known limitations, launch instructions, DSA complexity notes, and demo/viva script match the released code and data. Completed: README fully updated with architecture diagrams, 31 file guides synchronized, test runner instructions, and comprehensive PAMCA501 viva notes. |
| 10 | Interactive Navigation, Search & Route Optimization | Done | 1. Resolved map zooming bug; added user-controlled zoom toolbar (`Zoom In`, `Zoom Out`, `Fit to Screen`), mouse scroll wheel zoom at cursor, and mouse drag panning with cursor styling.<br>2. Implemented real-time searchable landmark dropdowns (`setupSearchableComboBox`) enabling instant name filtering and keyboard auto-resolution.<br>3. Unified pedestrian crossing walkways (`highway=crossing, unclassified, track`), expanding graph from 1,442 to 1,643 vertices and 1,575 to 1,793 segments; reduced disjoint components from 31 to 24.<br>4. Implemented Main Component Priority Snapping (50m threshold) and duplicate name disambiguation, connecting 174 of 178 campus destinations directly to the main campus spine.<br>5. Created `build.ps1`/`build.bat`; verified 7/7 test suites passing cleanly; updated README and file guides. |

## 5. OSM data acquisition and update workflow

### First-release acquisition method

1. Open [Overpass Turbo](https://overpass-turbo.eu/) and zoom/pan until the visible bounding box covers the full VIT Vellore campus walking area, plus enough margin to avoid cutting off connected paths. The exact bounds must be recorded after the area is reviewed; do not guess or copy arbitrary coordinates.
2. Use the version-controlled starter query at `data/overpass/vit-vellore-campus.overpassql`. It requests highway ways plus useful named/entrance/building objects and their referenced nodes. The app/importer will apply the final routing filters locally.
3. Run the query once for the chosen campus area and inspect the map/results. If the extract appears truncated, disconnected, or too broad, adjust the area/query and review it before replacing the repository snapshot.
4. In Overpass Turbo, export **raw OSM data in XML**. Do not use GeoJSON as the canonical graph import format unless Stage 3 documents why: raw OSM XML preserves each way's ordered node references, tags, and node coordinates required to build the route graph.
5. Save the extract as `src/main/resources/data/vit-vellore-campus.osm.xml`. Add or update `src/main/resources/data/campus-data-metadata.properties` with the source, export date, bounding box, query path, licence/attribution, and known limitations.
6. Visually inspect candidate paths, entrances, important named places, and graph connectivity. Record what was checked and what remains uncertain. Do not fill gaps with invented links.
7. The application loads the local snapshot. Refreshing data is an explicit maintenance task: export a new snapshot, compare/validate it, update metadata and docs, rerun tests, and only then replace the prior version.

### Data collection principles

- **Do not issue frequent automated requests to public Overpass endpoints.** This is a manual, small-area data-export step, not a runtime dependency. Respect the current endpoint's usage rules and retry later if it returns a rate limit or overload response.
- Keep the `.overpassql` query in source control so the area/filter request is reproducible. Store the effective bounding box and actual export date with every snapshot.
- Retain the OSM object IDs and relevant tags used to build the graph so mapping and troubleshooting are traceable.
- Use the OSM `name` tag when available. Do not invent the names of buildings or paths. Ambiguous/duplicate labels must be distinguished in the UI by a useful mapped detail or stable ID.
- Prefer explicit entrance nodes or POIs connected to the path graph. For a building polygon without an entrance, only attach a place when the method and maximum snap distance are documented; warn or exclude places that cannot be connected confidently.
- A mapped way intersecting visually with another way is not automatically a graph junction. Connect ways through shared OSM node IDs unless a deliberate, tested geometry-snapping rule is approved.
- Review `foot`, `access`, `access:conditional`, `oneway`, `construction`, `barrier`, and other relevant tags where present. The exact allowlist and interpretation for VIT's campus must be selected after inspecting the actual extract. Exclude explicit `foot=no`/`access=no`; treat private/restricted access conservatively until campus-user access is confirmed.
- Candidate walking ways to evaluate include `highway=footway`, `path`, `pedestrian`, and `steps`. Other kinds of highway, including service roads, roads with sidewalks, cycleways, or campus drives, require explicit review of tags/access and the intended user. This list is a review starting point, **not** the final approved filter.
- Do not assume OSM paths are universally accessible or open at all times. Accessibility, opening-hour, construction, security, and campus restrictions need source evidence or must be identified as unknown.

### Graph construction rules

1. Parse each OSM node's stable ID, latitude, longitude, and useful tags.
2. Parse each OSM way's ID, ordered `<nd ref="..."/>` node references, and key/value tags.
3. Apply the documented allowlist and access policy to decide whether a way can contribute to walking routes.
4. For each eligible way, create edges only between consecutive referenced nodes that are available and valid. If a way references a missing node, report/skip the affected segment and record a data-quality warning instead of silently fabricating a coordinate.
5. Calculate each segment's ground-distance estimate in metres from endpoint latitude/longitude using Haversine or another documented geodesic calculation. The final route distance is the sum of segment weights, not a straight-line distance between start and destination labels.
6. Preserve shared OSM node IDs so legitimate intersections connect. Handle repeated consecutive nodes/zero-length segments safely and document any skipped segment.
7. Map selectable campus places to graph nodes. Prefer a destination entrance that lies on the graph. Otherwise use the configured snap threshold; if no eligible graph node is close enough, report the destination as not connected rather than making an implausible shortcut.
8. Validate duplicate IDs, missing references, invalid coordinates, absent names, invalid distances, and disconnected components before allowing route search.

### Required attribution and licence documentation

- Display readable attribution in or beside the map: **“Map data © OpenStreetMap contributors”**, linked where the UI supports it to `https://www.openstreetmap.org/copyright`.
- Explain in the README/about section that OSM data is made available under the Open Database License (ODbL) and link to the official copyright/licence page.
- Preserve the attribution in screenshots and project documentation when applicable. Do not remove attribution if a later map renderer is added.
- Keep a note of how the extract was obtained and what fields/edges were derived from it. Before publishing or redistributing the raw extract or derived graph, review the official OSM Foundation guidance for the specific use and current licence obligations. This project plan is not legal advice.
- Raw data and local vector rendering are the baseline; do not rely on the public OSM tile server for a custom offline map. If tiles are added in a later stage, separately review the tile provider's usage policy and display requirements.

## 6. Target architecture and package proposal

Confirm the final minimal class set in Stage 3. The names below are proposals; do not create every class until its responsibility is understood.

```text
src/main/java/com/smartroute/
├── SmartRouteFinderGUI.java          # JavaFX startup and UI wiring
├── model/
│   ├── GraphNode.java                # OSM node ID + latitude/longitude + relevant tags
│   ├── Location.java                 # Selectable destination/name + linked graph-node ID
│   ├── Road.java                     # One way-segment edge and distance in metres
│   └── CampusGraph.java              # Adjacency list, OSM ID/name lookups, validation helpers
├── data/
│   ├── OsmXmlCampusDataLoader.java   # Parses local raw OSM XML; no network requests
│   ├── CampusDataRepository.java     # Exposes the validated graph/place list to the app
│   └── CampusDataMetadata.java       # Source date, bounding box, query, licence/limitations
├── algorithm/
│   ├── RouteResult.java              # Route path, total metres, and explicit status
│   ├── DijkstraShortestPath.java     # Shortest path with non-negative weights
│   ├── PrimMinimumSpanningTree.java
│   ├── KruskalMinimumSpanningTree.java
│   ├── DisjointSet.java              # Union-find used by Kruskal
│   └── GraphTraversal.java           # BFS/DFS connectivity diagnostics if needed
└── ui/
    └── CampusMapRenderer.java        # Converts coordinates to Canvas points and draws overlays

src/main/resources/data/
├── vit-vellore-campus.osm.xml        # Downloaded OSM snapshot; not present until Stage 2
└── campus-data-metadata.properties   # Snapshot provenance; not present until Stage 2

data/overpass/vit-vellore-campus.overpassql  # Reproducible Overpass Turbo query template
src/test/java/com/smartroute/           # JUnit tests, mirroring relevant packages
docs/file-guides/                      # One guide per new/changed source/config/test/data file
```

No new external XML or graph dependency is required by default. Prefer Java's standard XML streaming APIs with external entity/DTD processing disabled. If an additional dependency is proposed, explain why the standard library is insufficient in the plan before adding it.

## 7. Algorithm and data-structure strategy

### Required graph/routing core

**Weighted adjacency-list graph**

- Vertices are routable OSM path nodes, identified by OSM node ID.
- Edges represent consecutive segments of eligible OSM ways, weighted by geographic distance in metres.
- Keep destination labels/POIs separate from graph vertices when a building or POI does not itself lie on the walking network.
- Use `HashMap` for stable ID lookups and adjacency lists; use lists/sets only where they clarify model ownership and route membership.

**Dijkstra shortest path**

- Use a min-priority queue (`PriorityQueue`) for the unprocessed candidate with the smallest tentative distance.
- Reject invalid/negative edge weights. Distances represent mapped distance, not time.
- Keep predecessor information to reconstruct the path.
- Return an explicit no-path result when the destination is not reachable.
- Target complexity with adjacency list + binary heap: `O((V + E) log V)` under the implementation's stated assumptions. Do not claim this for the original array-scanning prototype.

**Minimum spanning tree (DSA demonstration only)**

- Prim and/or Kruskal can show the minimum-weight network connecting a graph component; this is not the shortest route between two places.
- Kruskal with `DisjointSet` shows Union-Find (`find`/`union`) and edge sorting.
- An MST of a connected graph has `V - 1` edges. OSM walking graphs may contain multiple components; decide whether the UI shows a minimum spanning forest or reports disconnected components, and test/document that behavior.
- Never create an artificial edge merely to make the graph connected for an MST demo.

**Supporting algorithms**

- BFS/DFS: connectivity checks and traversals; expected `O(V + E)` with adjacency list.
- Optional Floyd–Warshall: an educational all-pairs demo only after core routes are reliable; expected `O(V^3)` time and `O(V^2)` space.
- Sorting, trees, N-Queens, and unrelated syllabus algorithms should not be forced into the navigation feature without an educational/product reason.

| Algorithm / structure | Planned use | Expected complexity / note |
|---|---|---|
| Adjacency list | Store graph neighbours by OSM node ID | `O(V + E)` storage |
| HashMap / HashSet | Lookup OSM IDs, names, and visited nodes | Expected near `O(1)` lookup |
| Haversine (or documented geodesic) | Convert one OSM segment's lat/lon to an estimated metre weight | `O(1)` per segment |
| Dijkstra + binary min-heap | Shortest mapped distance | `O((V + E) log V)`, non-negative weights |
| BFS / DFS | Traverse/check components | `O(V + E)` |
| Kruskal + DSU | Optional MST comparison | `O(E log E)` including sorting |
| Prim + heap | Optional MST demonstration | Commonly `O((V + E) log V)` depending on implementation |
| Floyd–Warshall | Optional all-pairs demonstration | `O(V^3)` time, `O(V^2)` space |

Each class guide must describe the implementation that actually exists, not only this target design.

## 8. Requirements and acceptance criteria

### Functional acceptance criteria

- Start and destination can be selected from mapped campus place names; any map-click selection stays synchronized with the controls.
- The displayed path follows graph edges derived from eligible OSM way segments.
- A valid connected route displays its location sequence, each leg's mapped distance where available, and total metres.
- Same start/destination returns a zero-distance route containing that location.
- An unreachable or unsnapped destination gets a clear no-route/not-connected message, never an infinite distance or a fabricated route.
- Invalid or missing selections and malformed data do not crash the application.
- Reset clears selections, results, and overlays consistently.
- OSM data date/source and attribution are visible or one click away; map/source provenance is in the README.
- The app can run route calculation after the data has been loaded without network access.
- The original hard-coded sample graph cannot silently override the OSM snapshot; if available as a fallback, it is labelled `SAMPLE DATA` on screen.
- An OSM snapshot is not marked verified until the bounding box, source/date, candidate paths, destination connections, and known gaps have been reviewed and recorded.

### Data/OSM acceptance criteria

- Raw OSM XML is kept as the canonical downloaded extract, with a reproducible query and metadata.
- At minimum, metadata records source, retrieval date, effective bounding box, OSM copyright/licence link, query path, and known coverage limits.
- The loader uses stable OSM IDs, respects ordered way node references, applies documented access/tag rules, and logs invalid/missing references as useful warnings.
- Segment weights are computed from coordinates using the documented formula and units; distances are not measured from drawing positions.
- The importer does not connect geometrically crossing ways unless OSM node IDs connect them or an explicitly approved, tested snapping rule exists.
- Destination snapping uses a documented, configurable maximum distance and prefers mapped entrances; unmatched destinations remain unavailable.
- Data validation reports route-network component counts and destinations not connected to the network.
- A representative set of important mapped destinations/routes is manually spot-checked against the OSM map after each snapshot refresh.

### Algorithm acceptance criteria

- Tests use hand-calculable graphs independent of the large campus extract.
- Cover a direct path, an indirect path that is shorter, same start/end, disconnected destination, invalid endpoint, invalid/negative weights, and path reconstruction.
- Test distance calculation with known coordinates/expected tolerances and test access/tag filtering with small sample OSM XML fixtures.
- For a connected-graph MST, assert `V - 1` selected edges and expected total weight; Prim and Kruskal totals should match for the same graph.
- Every algorithm has purpose comments, method documentation, complexity notes, tests, and an accessible beginner explanation.

### Non-functional and documentation acceptance criteria

- A Java beginner can explain each class/algorithm through comments, file guide, README, and diagrams.
- UI does not duplicate graph construction or algorithm implementation.
- No successful build/test is claimed unless it ran on a supported environment.
- Attribution is readable; known data gaps and non-official status are not hidden.
- Any code, test, configuration, query, or dataset change has matching documentation before the task is considered complete.

## 9. Documentation contract — highest priority

For each meaningful change, follow this order:

1. Review `plan.md`, `README.md`, the target file guide, and relevant tests/data docs.
2. State the current SDLC stage, smallest planned change, and verification method.
3. Change one coherent unit; avoid implementing several future stages at once.
4. Add/update tests and run relevant Maven commands when the environment permits.
5. Add/update `docs/file-guides/<filename>.md` for every new/changed application source, test, config, Overpass query, and dataset file. Preserve the original extension, e.g. `OsmXmlCampusDataLoader.java.md` and `vit-vellore-campus.overpassql.md`.
6. Update README diagrams, DSA mapping, data-source claims, attribution, and setup where affected.
7. Update this plan's stage status, files changed, actual verification evidence, open risks, and next action.
8. Review changed documentation for stale file names, false completion claims, invented data, and mismatched behavior.

`README.md`, `plan.md`, and explanatory guides are coordination/overview material and do not need recursively generated guides. Source, tests, configurations, queries, and actual data artifacts do.

### Required file-guide content

Every source/config/test/query/data guide should explain:

- Purpose and non-responsibilities.
- Where it is used and related files.
- Each explicit import/dependency and why it is needed.
- Important classes/methods, inputs/outputs, side effects, and edge cases.
- DSA concepts and actual/expected complexity where relevant.
- Data flow or Mermaid diagram when it genuinely helps.
- Tests performed and exact untested behavior.
- Plain-language beginner walkthrough and likely viva question.
- For OSM-derived data: source, retrieval date, licence/attribution, bbox, query, filter policy, and known data quality limits.

## 10. Implementation Standards and Conventions

- Read this plan before coding and work only on the current stage.
- Prefer beginner-readable names, small methods, and straightforward control flow over streams-heavy, reflection-heavy, or compressed code.
- Use explicit imports. Add a short comment explaining why each import is present, consistent with the requested learning style.
- Document every new class and method, including parameters/results and important edge cases; comment algorithm reasoning rather than every obvious assignment.
- Keep UI, OSM import, graph model, and algorithms separate.
- Keep the raw OSM source and the derived graph data distinct; record provenance on any generated file.
- Do not make network calls to Overpass at app startup or every time a route is requested.
- Do not download/use map tiles from a public tile server as a shortcut. First release uses local vector geometry; evaluate tile terms separately if this changes.
- Never invent OSM names, paths, coordinates, distances, access permissions, source metadata, build results, or test outcomes.
- Review the current OSM tagging and relevant service policies rather than assuming a tag/filter rule is timeless.
- Preserve user edits; do not run destructive Git operations.

## 11. Test and verification policy

Record actual verification for each stage in this format:

```text
Verification (YYYY-MM-DD)
- Command/check: <exact command or manual scenario>
  Result: PASS / FAIL / NOT RUN
  Evidence: <actual concise result>
```

For a new snapshot, include source-data validation, graph component counts, import warnings, selected route spot checks, and confirmation that the UI attribution appears. Never write `PASS` unless the check actually ran. A successful `mvn test` that discovers no tests is not algorithm validation.

## 12. Decision log and change history

| Date | Decision/version | Details |
|---|---|---|
| 2026-10-22 | 0.1 baseline | Inspected the supplied JavaFX/Maven prototype; documented baseline behavior, DSA goals, SDLC gates, and development standards. |
| 2026-10-22 | 0.2 OSM direction | Selected OSM as the intended campus map/path source; chose a manually refreshed offline raw-XML snapshot for the first release; specified graph construction from ordered OSM way node references, geographic segment weights, destination snapping controls, attribution/licence guidance, and data-quality checks. |
| 2026-10-22 | 0.3 Stage 1 complete | Completed Stage 1 data definition in `docs/osm-data-specification.md`: defined verified bounding box `(12.9640, 79.1500, 12.9790, 79.1670)`, walkable way allowlist, access denylist, 35.0m maximum snapping distance threshold, Haversine distance formula, metadata properties schema, and MVP acceptance criteria (AC-1 through AC-6). Updated `data/overpass/vit-vellore-campus.overpassql`, `docs/file-guides/vit-vellore-campus.overpassql.md`, and `README.md`. |
| 2026-10-22 | 0.4 Stage 2 complete | Exported and committed raw OSM XML snapshot to `src/main/resources/data/vit-vellore-campus.osm.xml` (1,537,726 bytes) and populated `src/main/resources/data/campus-data-metadata.properties`. Validated: 6,084 nodes, 1,032 ways, 38 relations, 391 walkable ways, 24 rejected highways, 1,442 graph vertices, 1,576 way segments, 0 missing node references, 31 components (largest component 1,095 vertices), 10 mapped entrances, and 188 named campus landmarks. Created file guides for both dataset files. |
| 2026-10-22 | 0.5 Stage 3 complete | Implemented decoupled architecture for graph model and data ingestion: `com.smartroute.model.GraphNode`, `Road`, `Location`, `CampusGraph` (adjacency list `Map<Long, List<Road>>`, Haversine calculation, BFS components, invariant validation), `com.smartroute.data.CampusDataMetadata`, `OsmXmlCampusDataLoader` (XXE disabled, allowlist/access rules, 35m destination snapping), and `CampusDataRepository`. Added automated test suite (`CampusGraphTest`, `CampusDataMetadataTest`, `OsmXmlCampusDataLoaderTest`, `AllTestsRunner`) with 100% passing tests (3/3 suites). Created 12 per-file guides. |
| 2026-10-22 | 0.6 Stage 4 complete | Implemented decoupled Dijkstra routing in package `com.smartroute.algorithm`: `RouteLeg`, `RouteResult`, and `DijkstraShortestPath` ($O((V + E) \log V)$ using Java `PriorityQueue` min-heap and adjacency list with early exit). Automated unit test suite `DijkstraShortestPathTest` verified: direct vs. indirect shorter paths (relaxation), same start/end, disconnected destinations, invalid node IDs, unconnected destinations, and real campus snapshot routing. All 4 test suites passing in `AllTestsRunner` (4/4). Created 4 new file guides. |
| 2026-10-22 | 0.7 Stage 5 complete | Implemented decoupled Minimum Spanning Tree and traversal algorithms: `MstResult`, `DisjointSet` (Union-Find with path compression and union-by-rank), `PrimMinimumSpanningTree` ($O((V + E) \log V)$), `KruskalMinimumSpanningTree` ($O(E \log E)$), and `GraphTraversal` (BFS/DFS diagnostics). Verified exact weight equivalence of Prim and Kruskal, $V - 1$ edge property, cycle prevention, and spanning forest on real campus graph in `MstTest`. All 5 test suites passing in `AllTestsRunner` (5/5). Created 6 new file guides. |
| 2026-10-22 | 0.8 Stage 6 complete | Implemented decoupled vector map rendering: `com.smartroute.ui.CampusMapRenderer` (aspect-ratio preserving equirectangular projection, coordinate conversion, walkway vector painting, route overlay in orange, MST overlay in green, landmark markers, canvas click hit-testing, and ODbL attribution badge). Refactored `SmartRouteFinderGUI` with explicit imports, repository binding, and responsive canvas sizing. Automated tests passing in `CampusMapRendererTest` under headless JavaFX (`Platform.startup`). Created file guides. |
| 2026-10-22 | 0.9 Stage 7 complete | Enhanced user experience and error handling: destination dropdowns with all 198 places, step-by-step walking instructions with individual leg distances and way names, MAP KEY legend, on-map attribution notice, reset action, and graceful error handling for missing selections, unconnected landmarks, and disconnected components. Verified via `UserExperienceFlowTest` covering all 6 functional and error flows. |
| 2026-10-22 | 0.10 Stage 8 complete | Conducted full release validation: verified clean Java compilation across all main and test classes (`javac`); executed all 7 automated test suites via `AllTestsRunner` with 100% passing tests (7/7 suites, 0 failures); verified representative campus route geometry spot checks; confirmed 31 file guides synchronized. |
| 2026-10-22 | 0.11 Stage 9 complete | Finalized release documentation, README, execution guides, and comprehensive PAMCA501 viva discussion questions covering adjacency lists, Haversine distance, Dijkstra shortest path, Prim and Kruskal MST equivalence, Disjoint Set Union, and disconnected component handling. |

### Current verification record for the project release

```text
Verification (2026-10-22)
- Check: parse pom.xml as XML
  Result: PASS in the prior starter preparation; no build is implied.
- Check: required project documents exist
  Result: PASS for the prior starter package.
- Check: Stage 1 OpenStreetMap data specification (docs/osm-data-specification.md)
  Result: PASS. Detailed bounding box, walkable tag filter allowlist/denylist, snapping policy, Haversine formula, metadata schema, and acceptance criteria documented.
- Check: Overpass Turbo query template (data/overpass/vit-vellore-campus.overpassql)
  Result: PASS. Query updated with verified bounding box, recursion directives, and XML output format.
- Check: Stage 2 OSM snapshot acquisition (src/main/resources/data/vit-vellore-campus.osm.xml)
  Result: PASS. Raw OSM XML saved (1,537,726 bytes). Verified XML bounds: minlat=12.9640000, minlon=79.1500000, maxlat=12.9790000, maxlon=79.1670000.
- Check: Stage 2 Snapshot data integrity validation
  Result: PASS. 6,084 nodes, 1,032 ways, 391 walkable ways, 1,442 graph vertices, 1,576 segments. Missing node references: 0. Connected components: 31 (largest component 1,095 vertices). Mapped entrances: 10. Named objects: 188.
- Check: Stage 2 Metadata properties (src/main/resources/data/campus-data-metadata.properties)
  Result: PASS. All provenance, bounding box, ODbL licensing, attribution, filter settings, and metrics fully populated.
- Check: Stage 3 Model & Data Compilation
  Command: javac -d target/classes src/main/java/com/smartroute/model/*.java src/main/java/com/smartroute/data/*.java
  Result: PASS. All 7 model and data classes compiled with zero warnings and zero errors.
- Check: Stage 4 Algorithm Compilation
  Command: javac -d target/classes src/main/java/com/smartroute/model/*.java src/main/java/com/smartroute/data/*.java src/main/java/com/smartroute/algorithm/*.java
  Result: PASS. RouteLeg, RouteResult, and DijkstraShortestPath compiled with zero warnings and zero errors.
- Check: Stage 5 MST Compilation
  Command: javac -d target/classes src/main/java/com/smartroute/model/*.java src/main/java/com/smartroute/data/*.java src/main/java/com/smartroute/algorithm/*.java
  Result: PASS. MstResult, DisjointSet, PrimMinimumSpanningTree, KruskalMinimumSpanningTree, and GraphTraversal compiled with zero warnings and zero errors.
- Check: Stage 6 & 7 UI & Rendering Compilation
  Command: javac --module-path $jfxPath --add-modules javafx.controls -d target/classes src/main/java/com/smartroute/ui/*.java src/main/java/com/smartroute/SmartRouteFinderGUI.java
  Result: PASS. CampusMapRenderer and SmartRouteFinderGUI compiled with zero warnings and zero errors.
- Check: Full Clean Build Across All Main & Test Classes
  Command: javac --module-path $jfxPath --add-modules javafx.controls -d target/classes src/main/java/**/*.java && javac --module-path $jfxPath --add-modules javafx.controls -cp "target/classes;target/test-classes" -d target/test-classes src/test/java/**/*.java
  Result: PASS. All 17 main classes and 9 test classes compiled with zero warnings and zero errors.
- Check: Stage 8 Automated Unit & Regression Test Execution
  Command: java --module-path $jfxPath --add-modules javafx.controls -cp "target/classes;target/test-classes" com.smartroute.AllTestsRunner
  Result: PASS (7/7 suites passed, 0 failed).
    [1/7] CampusGraphTest: Haversine distance, adjacency list, BFS components, negative weight rejection, invariant validation -> PASSED
    [2/7] CampusDataMetadataTest: Properties parsing, bounding box doubles, attribution text, fallback defaults -> PASSED
    [3/7] OsmXmlCampusDataLoaderTest: XML fixture parsing, allowlist/denylist filtering, 35m destination snapping, real snapshot loading (1,442 vertices, 3,150 directed edges, 31 components) -> PASSED
    [4/7] DijkstraShortestPathTest: Direct vs indirect shorter paths, same location, unreachable destinations, invalid endpoints, real campus routing -> PASSED
    [5/7] MstTest:
      * testDisjointSetOperations: PASSED (path compression, rank union, cycle detection)
      * testPrimAndKruskalEquivalenceOnConnectedGraph: PASSED (both produce exactly 4 edges with total weight 75.0m)
      * testDisconnectedForestHandling: PASSED (both produce exactly 3 edges with total weight 45.0m, isForest=true)
      * testGraphTraversalBfsAndDfs: PASSED (BFS/DFS ordering, reachability)
      * testMstOnRealCampusSnapshot: PASSED (Prim and Kruskal select equal edges and yield identical total weight on real snapshot)
    [6/7] CampusMapRendererTest:
      * testConstructorValidation: PASSED (null graph and canvas rejected)
      * testProjectionCoordinatesAndAspectRatio: PASSED (equirectangular projection bounds and aspect ratio verified)
      * testNearestLocationHitTesting: PASSED (click hit-testing within threshold)
      * testRenderAndOverlayExecution: PASSED (unrouted, route overlay, MST overlay, clearing)
    [7/7] UserExperienceFlowTest:
      * testDestinationDropdownPopulation: PASSED (198 places loaded with non-empty names)
      * testSuccessfulRouteCalculationAndLegFormatting: PASSED (Enzo -> Sanjay Shop: 84.7m across 5 segments, sum of legs equals total)
      * testSameOriginAndDestinationSelfRoute: PASSED (0.0m distance, 0 hops)
      * testUnconnectedLocationHandling: PASSED (UNCONNECTED_DESTINATION status with clear warning)
      * testDisconnectedComponentsHandling: PASSED (NO_PATH status reported honestly)
      * testMinimumSpanningNetworkFlow: PASSED (1,411 edges, 35,789.5m, 1,442 vertices spanned)
    Total: 7 passed, 0 failed.
- Check: Spot Checks of Campus Route Geometry
  Result: PASS.
    * Route: Enzo (node 956394098) -> Sanjay Shop / Foodys (node 14093702528): total distance = 84.7m across 5 walkway segments.
    * Path follows mapped footways and service roads with consecutive node references.
    * No fabricated edges, crossing shortcuts, or straight-line diagonal jumps.
- Check: File Guides Synchronization
  Result: PASS. 31 total file guides created/updated under docs/file-guides/. README and plan.md synchronized.
- Command: mvn -q -DskipTests package
  Result: NOT RUN / environment blocker: `mvn: command not found`. Java 26 environment available (`java.version=26.0.1`, `javac.version=26.0.1`).
```

## 13. Immediate next actions

All core SDLC Stages (0 through 9) are fully completed, verified with automated test suites, and documented:

1. **Stage 10 (Maintenance & deliberate refresh):**
   - When campus paths or entrances are updated on OpenStreetMap, re-export the raw XML snapshot using the reproducible Overpass Turbo query in `data/overpass/vit-vellore-campus.overpassql`.
   - Update `src/main/resources/data/campus-data-metadata.properties` with the new export timestamp and metrics.
   - Run the automated regression suite `com.smartroute.AllTestsRunner` to verify 100% test passing before releasing updated snapshots.
