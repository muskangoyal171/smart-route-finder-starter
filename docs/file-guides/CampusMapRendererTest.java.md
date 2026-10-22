# `CampusMapRendererTest.java` — File Guide

## Purpose

`CampusMapRendererTest` is an automated test suite located in `src/test/java/com/smartroute/ui/CampusMapRendererTest.java`. It verifies the correctness and stability of the vector rendering and coordinate projection system in `CampusMapRenderer` without needing a GUI display.

## Where It Is Used

- **Package:** `com.smartroute.ui`
- **Execution:** Run as part of the complete test suite via `AllTestsRunner` (which calls `CampusMapRendererTest.runTests()`) or individually using `java com.smartroute.ui.CampusMapRendererTest` (whose `main` invokes `runTests()` and cleanly terminates the JavaFX process).
- **JavaFX Headless Support:** Leverages `Platform.startup(...)` with a synchronized `CountDownLatch` to initialize the JavaFX platform for headless `Canvas` and `GraphicsContext` testing.

## Explicit Imports and Purpose

- `com.smartroute.data.CampusDataMetadata`: Provides bounding box limits and provenance metadata.
- `com.smartroute.data.CampusDataRepository`: Loads the validated graph and places.
- `com.smartroute.model.CampusGraph`: Graph model holding vertices, edges, and destinations.
- `com.smartroute.model.GraphNode`: Vertex model with geographic coordinates.
- `com.smartroute.model.Location`: Selectable campus landmark model.
- `com.smartroute.model.Road`: Weighted road edge model.
- `com.smartroute.testutil.SimpleAssert`: Lightweight assertion utility.
- `java.util.ArrayList`: Dynamic list for node IDs and edges.
- `java.util.List`: List interface.
- `java.util.concurrent.CountDownLatch`: Thread barrier synchronizing JavaFX platform initialization.
- `java.util.concurrent.TimeUnit`: Timeout unit for countdown latch wait.
- `javafx.application.Platform`: JavaFX lifecycle manager for background toolkit initialization.
- `javafx.scene.canvas.Canvas`: Drawing surface on which rendering is tested.

## Test Scenarios Covered

1. **`testConstructorValidation()`:**
   - Verifies that passing a null `CampusGraph` or null `Canvas` immediately throws an `IllegalArgumentException`.

2. **`testProjectionCoordinatesAndAspectRatio()`:**
   - Verifies equirectangular coordinate projection math.
   - Ensures Western longitudes map to smaller X pixel coordinates than Eastern longitudes ($X_{\text{west}} < X_{\text{east}}$).
   - Ensures Northern latitudes map to smaller Y pixel coordinates than Southern latitudes ($Y_{\text{north}} < Y_{\text{south}}$), respecting the inverted JavaFX Y-axis.
   - Verifies that all bounding box edges remain strictly within the padded canvas boundary ($X \in [35.0, 815.0]$, $Y \in [35.0, 545.0]$).

3. **`testNearestLocationHitTesting()`:**
   - Simulates mouse click events by translating geographic coordinates into canvas pixels.
   - Tests exact click hit-testing, validating that the nearest landmark matches within 1 pixel.
   - Tests offset click hit-testing within a 15-pixel threshold.
   - Tests that clicks outside the threshold ($> 100\text{ px}$) return null or ignore the landmark.

4. **`testRenderAndOverlayExecution()`:**
   - Exercises all map painting modes to guarantee no `NullPointerException` or rendering exceptions occur:
     - Initial unrouted canvas draw.
     - Selected origin and destination markers.
     - Highlighted Dijkstra route overlay.
     - Highlighted Prim minimum spanning tree/forest overlay.
     - Clearing overlays.

## Viva Discussion Points

- *How can JavaFX rendering be tested automatically without opening a window?*  
  By invoking `Platform.startup(() -> {})` once in headless mode, JavaFX's graphics pipeline and `Canvas` context become available in memory, allowing vector drawing, bounds calculations, and hit-testing to run headlessly in test runners.

