# `SmartRouteFinderGUI.java` — File Guide

## Purpose

`SmartRouteFinderGUI` is the JavaFX desktop application entry point for the VIT Vellore Smart Route Finder. It provides an interactive graphical interface coordinating user input (searchable origin/destination dropdowns with auto-complete and canvas point-and-click selection), interactive map navigation (toolbar zoom buttons, mouse wheel zooming, and mouse drag panning), route and network calculation via decoupled algorithm services (`DijkstraShortestPath`, `PrimMinimumSpanningTree`), and vector map rendering via `CampusMapRenderer`.

It replaces the monolithic starter prototype with a modular, decoupled architecture where data loading, graph storage, algorithms, and rendering are separated cleanly.

## Where it is used

- **Application Entry Point:** Declared as the main class in `pom.xml` (`com.smartroute.SmartRouteFinderGUI`) and executed via `SmartRouteFinderGUI.main(String[])` or the `run.ps1` / `run.bat` launch scripts.
- **UI Controller:** Hosts JavaFX controls, listens for user events, coordinates data flow between `CampusDataRepository`, `CampusGraph`, `DijkstraShortestPath`, `PrimMinimumSpanningTree`, and `CampusMapRenderer`.

## Imports and why they are used

Explicit imports are organized by package with individual inline rationale comments:

### JavaFX Lifecycle, Layout, and Controls
- `javafx.application.Application`: Base class providing the JavaFX application lifecycle (`start`, `stop`, `launch`).
- `javafx.application.Platform`: Executes runnables on the JavaFX application UI thread (used for searchable ComboBox dynamic filtering).
- `javafx.collections.FXCollections`: Observable collection factory creating backing lists for the dropdown controls.
- `javafx.collections.ObservableList`: Observable list backing ComboBox items.
- `javafx.collections.transformation.FilteredList`: Dynamically filtered list wrapping location names to support real-time typing search.
- `javafx.geometry.Insets`: Defines padding and margins around panels and canvas elements.
- `javafx.geometry.Pos`: Layout alignment positioning for toolbar controls.
- `javafx.scene.Cursor`: Changes mouse cursor appearance (e.g. `HAND` on hover, `CLOSED_HAND` during drag-panning).
- `javafx.scene.Scene`: Container holding the visual scene graph attached to the application stage.
- `javafx.scene.canvas.Canvas`: Drawing surface hosting the vector rendering of campus ways, landmarks, and route/MST overlays.
- `javafx.scene.control.Button`: Action buttons triggering "Find Shortest Route", "Show Minimum Network", "Reset", "Zoom In", "Zoom Out", and "Fit to Screen".
- `javafx.scene.control.ComboBox`: Searchable, editable dropdown selectors for start and destination campus locations.
- `javafx.scene.control.Label`: Text controls displaying status messages, navigation hints, instructions, route leg breakdowns, and map legends.
- `javafx.scene.control.Separator`: Visual dividing lines between sidebar control groups and toolbar items.
- `javafx.scene.control.TextField`: Embedded text field editor inside editable ComboBox controls.
- `javafx.scene.control.Tooltip`: Informational hover tooltips for zoom toolbar buttons.
- `javafx.scene.layout.BorderPane`: Primary root layout dividing the window into controls (left), map container (center), and status bar (bottom).
- `javafx.scene.layout.HBox`: Horizontal toolbar container hosting map zoom and navigation controls.
- `javafx.scene.layout.Priority`: Controls layout resizing behavior when the window geometry changes.
- `javafx.scene.layout.VBox`: Vertical layout container structuring sidebar controls and information readouts.
- `javafx.stage.Stage`: Top-level window hosting the application scene.

### Domain Model, Algorithms, and Data Layer
- `com.smartroute.algorithm.DijkstraShortestPath`: Single-source shortest path algorithm using a min-heap priority queue over the campus graph.
- `com.smartroute.algorithm.MstResult`: Immutable value container encapsulating minimum spanning tree/forest edges, weight, and component statistics.
- `com.smartroute.algorithm.PrimMinimumSpanningTree`: Greedy minimum spanning tree algorithm generating the lowest-cost campus network.
- `com.smartroute.algorithm.RouteLeg`: Value object representing an individual step along a path with distance and way name.
- `com.smartroute.algorithm.RouteResult`: Detailed result container holding path status, total distance, hop count, node IDs, and legs.
- `com.smartroute.data.CampusDataMetadata`: Holds dataset provenance, bounding coordinates, extraction timestamp, and ODbL attribution.
- `com.smartroute.data.CampusDataRepository`: High-level data accessor loading and caching the parsed campus graph and metadata.
- `com.smartroute.model.CampusGraph`: Decoupled adjacency-list graph representing physical campus ways and snapped destinations.
- `com.smartroute.model.Location`: Selectable campus landmark with geographic coordinates, mapped entrance node, and snap status.
- `com.smartroute.ui.CampusMapRenderer`: Dedicated canvas vector rendering engine handling projection, coordinate transformation, zoom/pan states, and overlays.

### Utilities
- `java.util.ArrayList`: Dynamic list implementation for storing landmark names and route segments.
- `java.util.List`: Interface for accessing ordered collections of locations, path nodes, and route legs.
- `java.util.function.Consumer`: Functional callback interface invoked when a landmark is committed or selected.

## Important fields and methods

| Name | Role |
|---|---|
| `repository` | Cached instance of `CampusDataRepository` providing the graph and metadata. |
| `graph` | Loaded `CampusGraph` containing 1,643 vertices and 1,793 walkable segments (3,586 directed edges). |
| `metadata` | Loaded `CampusDataMetadata` containing dataset provenance, bounding box, and ODbL attribution. |
| `mapRenderer` | Dedicated `CampusMapRenderer` performing aspect-ratio equirectangular projection, zoom/pan scaling, and vector painting onto `mapCanvas`. |
| `startLocation`, `destinationLocation` | Current user-selected origin and destination `Location` instances. |
| `mapCanvas` | Responsive `Canvas` element displaying vector map geometry. |
| `statusLabel` | Bottom status bar reporting dataset loading state, vertex/edge counts, and active route distance. |
| `resultLabel` | Left sidebar panel displaying step-by-step route directions or MST summary statistics. |
| `startBox`, `endBox` | Searchable editable ComboBox controls populated with all 198 mapped campus landmarks. |
| `dragStartX`, `dragStartY`, `pressStartX`, `pressStartY` | Mouse tracking coordinates enabling smooth drag-panning without accidental landmark click selection. |
| `start(Stage)` | JavaFX lifecycle entry point; initializes data repository, builds controls sidebar, configures zoom toolbar, attaches mouse listeners, and paints the initial map. |
| `setupSearchableComboBox(...)` | Configures an editable ComboBox with a `FilteredList` listener so typing filters landmarks in real time. |
| `attachSpaceFilter(...)` | Attaches a key event filter to the ComboBox popup ListView consuming `SPACE` so typing a space does not prematurely trigger item selection or close the popup. |
| `commitSelection(...)` | Resolves typed input against canonical campus landmarks using exact and partial name matching. |
| `selectLocationByClick(double, double)` | Canvas mouse click handler; identifies nearest landmark within a 25-pixel threshold and assigns start/destination alternately. |
| `findRoute()` | Validates selections, executes `DijkstraShortestPath`, updates map route overlay, and renders step-by-step walking instructions. |
| `showMinimumNetwork()` | Executes `PrimMinimumSpanningTree`, updates map green overlay, and reports spanned vertices and total network length. |
| `reset()` | Clears origin, destination, dropdown selections, route/MST overlays, resets map zoom and pan, and resets status messages. |
| `main(String[])` | Standard Java entry point invoking `Application.launch(args)`. |

## DSA and UI Concepts

- **Searchable Dropdown Filtering & Multi-Word Spaces:** Uses `FilteredList<String>` on top of `ObservableList<String>`. As the user types, input is split into whitespace-delimited search terms (`\\s+`), matching all multi-word campus names (e.g. `"Food Court 1"`, `"EV Periyar Library"`). An event filter on the popup's internal `ListView` consumes `KeyCode.SPACE` during capturing, preventing JavaFX's `ComboBoxListViewSkin` from misinterpreting the spacebar as an item selection shortcut, ensuring uninterrupted typing.
- **Interactive Viewport Transformation:** Coordinates are transformed through equirectangular projection and scaled by `zoomFactor` (clamped between 0.5x and 10.0x) with pan offsets `(panOffsetX, panOffsetY)`.
- **Drag vs. Click Disambiguation:** Tracks Euclidean distance `Math.hypot(x - startX, y - startY)`. If the user drags more than 6 pixels, it is treated as a pan operation; if less, it is treated as a landmark selection click.
- **Shortest Path vs. Minimum Spanning Tree:** The GUI clearly distinguishes between point-to-point shortest route queries (Dijkstra) and global minimum connecting networks (Prim). A note in the UI explicitly educates students that an MST represents minimum infrastructure, not a point-to-point journey.
- **Robust Error Handling:** Unreachable destinations, self-loops, and unselected locations return clear, explanatory status messages rather than throwing exceptions or drawing invalid lines.

## Testing

Verified interactively and via automated headless test suites:
- All 7 test suites pass with 0 failures via `AllTestsRunner` (`.\test.ps1`).
