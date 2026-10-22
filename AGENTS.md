# Workspace Rules for Coding Agents

Before doing work, read `plan.md` (the source of truth), the relevant sections of `README.md`, `docs/campus-data-workflow.md` for data work, and the target file guide under `docs/file-guides/`.

## Order of priority

1. Keep documentation synchronized with the latest implementation and data. Documentation is a release gate, not optional cleanup.
2. Follow the current SDLC stage and its exit criteria in `plan.md`.
3. Keep the project beginner-friendly while implementing meaningful PAMCA501 DSA concepts.
4. Preserve the build and existing behavior unless an approved plan decision requires a change.
5. Do not claim the actual campus data has been integrated before a local OSM snapshot has been exported, validated, and loaded successfully.

## Required task workflow

- At the start of a task, name the current stage, smallest intended change, and how success will be verified.
- Make one small, coherent change. Do not implement future stages prematurely.
- Use explicit Java imports and a brief reason comment for each import. Document every new type and method with purpose, input/output, and important edge cases.
- Add/update tests for algorithm or data changes. Run relevant Maven commands and report actual outcomes only.
- Add/update `docs/file-guides/<filename>.md` for every new/changed application source, test, configuration, query, or dataset file. Examples: `CampusGraph.java.md`, `vit-vellore-campus.overpassql.md`.
- Update the README, Mermaid diagrams, `plan.md` stage status, files changed, test evidence, open risks, and next action in the same task.
- Never mark a stage Done until all its exit criteria have verifiable evidence.

## OpenStreetMap requirements

- Use a local, version-controlled OSM XML snapshot for the first release. Normal application start and route searches must not send requests to Overpass.
- Preserve OSM IDs, way-node order, tags, and source metadata. Use edges between consecutive way node references; never invent paths from nearby destination labels.
- Calculate road-segment distance from the segment's geographic coordinates in metres and document the formula. Drawing coordinates are not route weights.
- Review `highway`, `foot`, `access`, `barrier`, `construction`, entrance, and other relevant tags before implementing the final filter. Do not assume every `highway` object is a valid pedestrian way.
- Exclude explicit `foot=no` or `access=no`. Handle private/restricted routes conservatively and document the chosen campus-user access assumption. Do not claim accessibility or open hours unless the data supports it.
- Prefer mapped entrances and safe graph connections. Use a documented max snap distance for detached POIs; show unmatched places as unconnected instead of drawing a shortcut.
- Do not connect geometrically crossing paths unless shared OSM nodes connect them or a separately approved, tested rule is documented.
- Show readable “Map data © OpenStreetMap contributors” attribution linked to `https://www.openstreetmap.org/copyright`; document ODbL obligations before public distribution.
- Do not use public raster map tiles without separately checking the tile provider's policy. The first release renders local vector geometry.
- Never invent bounding boxes, OSM names, paths, coordinate data, retrieval dates, access permissions, test results, or build results.

## Architecture and DSA boundaries

- Keep JavaFX UI/rendering separate from OSM parsing, graph storage, and algorithms.
- Use an adjacency list for the weighted graph unless `plan.md` records a justified alternative.
- Use Dijkstra only with non-negative edge weights and handle unreachable destinations explicitly.
- Do not confuse an MST with a point-to-point shortest route.
- Prefer standard Java collections; introduce a custom data structure only when it serves the DSA objective and is easy to explain.

## Guardrails

- Avoid wildcard imports, unexplained magic numbers, unnecessary frameworks, and syntax that is hard for a beginner to present.
- Preserve user-created changes and never run destructive Git commands.
- If code and documentation disagree, investigate and fix both before handing off.
