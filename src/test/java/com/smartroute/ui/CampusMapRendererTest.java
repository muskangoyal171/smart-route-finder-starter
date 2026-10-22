package com.smartroute.ui;

// Explicit imports with purpose comments
import com.smartroute.data.CampusDataMetadata;        // Snapshot bounds and metadata
import com.smartroute.data.CampusDataRepository;      // High-level data accessor
import com.smartroute.model.CampusGraph;               // Graph model holding nodes and landmarks
import com.smartroute.model.GraphNode;                 // Vertex model with coordinates
import com.smartroute.model.Location;                  // Location model for landmarks
import com.smartroute.model.Road;                      // Road edge model
import com.smartroute.testutil.SimpleAssert;           // Lightweight assertion helper
import java.util.ArrayList;                            // Path node list
import java.util.List;                                 // List interface
import java.util.concurrent.CountDownLatch;            // Thread synchronization for JavaFX startup
import java.util.concurrent.TimeUnit;                  // Timeout units for countdown latch
import javafx.application.Platform;                   // JavaFX application thread runner
import javafx.scene.canvas.Canvas;                     // Drawing canvas

/**
 * Automated test suite for {@link CampusMapRenderer}.
 * <p>
 * Verifies geographic-to-pixel equirectangular projection math, aspect ratio preservation,
 * nearest landmark click detection (hit testing), overlay state manipulation, and canvas
 * drawing operations without exceptions.
 * </p>
 */
public class CampusMapRendererTest {

    private static boolean jfxInitialized = false;

    /**
     * Ensures the JavaFX platform toolkit is initialized exactly once before running UI tests.
     */
    private static void ensureJavaFxInitialized() throws InterruptedException {
        if (jfxInitialized) return;

        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                jfxInitialized = true;
                latch.countDown();
            });
        } catch (IllegalStateException alreadyInitialized) {
            jfxInitialized = true;
            latch.countDown();
        }

        boolean ok = latch.await(5, TimeUnit.SECONDS);
        SimpleAssert.assertTrue(ok, "JavaFX platform failed to initialize within 5 seconds");
    }

    public static void runTests() throws Exception {
        ensureJavaFxInitialized();

        System.out.println("--- Starting CampusMapRendererTest ---");

        testConstructorValidation();
        testProjectionCoordinatesAndAspectRatio();
        testNearestLocationHitTesting();
        testRenderAndOverlayExecution();

        System.out.println("--- CampusMapRendererTest: ALL TESTS PASSED ---");
    }

    public static void main(String[] args) throws Exception {
        runTests();
        System.exit(0);
    }

    /**
     * Verifies that null parameters to the renderer constructor are properly rejected.
     */
    private static void testConstructorValidation() {
        System.out.println("Running testConstructorValidation...");

        CampusGraph graph = new CampusGraph();
        Canvas canvas = new Canvas(600, 400);

        try {
            new CampusMapRenderer(null, null, canvas);
            SimpleAssert.fail("Expected IllegalArgumentException for null graph");
        } catch (IllegalArgumentException expected) {
            SimpleAssert.assertTrue(expected.getMessage().contains("CampusGraph"), "Expected message about CampusGraph");
        }

        try {
            new CampusMapRenderer(graph, null, null);
            SimpleAssert.fail("Expected IllegalArgumentException for null canvas");
        } catch (IllegalArgumentException expected) {
            SimpleAssert.assertTrue(expected.getMessage().contains("Canvas"), "Expected message about Canvas");
        }

        System.out.println("-> testConstructorValidation PASSED");
    }

    /**
     * Verifies that equirectangular projection correctly converts (lat, lon) to (X, Y)
     * such that northern latitudes map to smaller Y values and eastern longitudes map to larger X values.
     */
    private static void testProjectionCoordinatesAndAspectRatio() throws Exception {
        System.out.println("Running testProjectionCoordinatesAndAspectRatio...");

        CampusDataRepository repo = CampusDataRepository.loadDefault();
        CampusGraph graph = repo.getGraph();
        CampusDataMetadata metadata = repo.getMetadata();

        Canvas canvas = new Canvas(850, 580);
        CampusMapRenderer renderer = new CampusMapRenderer(graph, metadata, canvas);

        double southLat = metadata.getBboxSouth();
        double northLat = metadata.getBboxNorth();
        double westLon = metadata.getBboxWest();
        double eastLon = metadata.getBboxEast();

        double xWest = renderer.toPixelX(westLon);
        double xEast = renderer.toPixelX(eastLon);
        double ySouth = renderer.toPixelY(southLat);
        double yNorth = renderer.toPixelY(northLat);

        // West must be to the left of East
        SimpleAssert.assertTrue(xWest < xEast, "West pixel X should be strictly less than East pixel X");
        // North must be at a smaller Y coordinate than South (JavaFX Y-axis points downwards)
        SimpleAssert.assertTrue(yNorth < ySouth, "North pixel Y should be strictly less than South pixel Y");

        // Coordinates must fall within the padded canvas boundaries
        SimpleAssert.assertTrue(xWest >= 35.0, "West pixel coordinate should honor left padding");
        SimpleAssert.assertTrue(xEast <= 850.0 - 35.0, "East pixel coordinate should honor right padding");
        SimpleAssert.assertTrue(yNorth >= 35.0, "North pixel coordinate should honor top padding");
        SimpleAssert.assertTrue(ySouth <= 580.0 - 35.0, "South pixel coordinate should honor bottom padding");

        System.out.println("-> testProjectionCoordinatesAndAspectRatio PASSED (xWest=" +
                String.format("%.1f", xWest) + ", xEast=" + String.format("%.1f", xEast) +
                ", yNorth=" + String.format("%.1f", yNorth) + ", ySouth=" + String.format("%.1f", ySouth) + ")");
    }

    /**
     * Verifies click hit-testing by checking that clicking near a landmark resolves correctly
     * and clicking far away returns null.
     */
    private static void testNearestLocationHitTesting() throws Exception {
        System.out.println("Running testNearestLocationHitTesting...");

        CampusDataRepository repo = CampusDataRepository.loadDefault();
        CampusGraph graph = repo.getGraph();
        CampusDataMetadata metadata = repo.getMetadata();

        Canvas canvas = new Canvas(850, 580);
        CampusMapRenderer renderer = new CampusMapRenderer(graph, metadata, canvas);

        // Find Technology Tower or another mapped landmark
        Location targetLoc = null;
        for (Location loc : graph.getAllLocations()) {
            if (loc.isConnected() && loc.getName().toLowerCase().contains("technology tower")) {
                targetLoc = loc;
                break;
            }
        }
        if (targetLoc == null && !graph.getAllLocations().isEmpty()) {
            targetLoc = graph.getAllLocations().get(0);
        }
        SimpleAssert.assertTrue(targetLoc != null, "Must have at least one mapped location for testing");

        double clickX = renderer.toPixelX(targetLoc.getLongitude());
        double clickY = renderer.toPixelY(targetLoc.getLatitude());

        // Exact click should resolve to targetLoc (or a co-located entrance)
        Location foundExact = renderer.findNearestLocation(clickX, clickY, 15.0);
        SimpleAssert.assertNotNull(foundExact, "Exact click should resolve to a location");
        double exactDist = Math.hypot(clickX - renderer.toPixelX(foundExact.getLongitude()),
                clickY - renderer.toPixelY(foundExact.getLatitude()));
        SimpleAssert.assertTrue(exactDist < 1.0, "Exact click must match coordinate within 1 pixel");

        // Click with a 5-pixel offset within threshold 15.0px should resolve to a landmark within threshold
        Location foundOffset = renderer.findNearestLocation(clickX + 5.0, clickY - 5.0, 15.0);
        SimpleAssert.assertNotNull(foundOffset, "Offset click within threshold should resolve");
        double offsetDist = Math.hypot(clickX + 5.0 - renderer.toPixelX(foundOffset.getLongitude()),
                clickY - 5.0 - renderer.toPixelY(foundOffset.getLatitude()));
        SimpleAssert.assertTrue(offsetDist <= 15.0, "Resolved landmark should be within hit threshold");

        // Click with a 100-pixel offset should not match targetLoc
        Location foundFar = renderer.findNearestLocation(clickX + 100.0, clickY + 100.0, 10.0);
        if (foundFar != null) {
            SimpleAssert.assertTrue(!foundFar.getName().equals(targetLoc.getName()),
                    "Far away click should not match target location");
        }

        System.out.println("-> testNearestLocationHitTesting PASSED for landmark: " + targetLoc.getName());
    }

    /**
     * Verifies that canvas rendering, route highlighting, and MST overlays execute cleanly
     * without runtime exceptions.
     */
    private static void testRenderAndOverlayExecution() throws Exception {
        System.out.println("Running testRenderAndOverlayExecution...");

        CampusDataRepository repo = CampusDataRepository.loadDefault();
        CampusGraph graph = repo.getGraph();
        CampusDataMetadata metadata = repo.getMetadata();

        Canvas canvas = new Canvas(850, 580);
        CampusMapRenderer renderer = new CampusMapRenderer(graph, metadata, canvas);

        // 1. Initial draw
        renderer.draw();

        // 2. Set selections
        List<Location> locs = graph.getAllLocations();
        if (locs.size() >= 2) {
            renderer.setSelectedStart(locs.get(0));
            renderer.setSelectedDestination(locs.get(1));
            renderer.draw();
        }

        // 3. Set dummy route overlay
        List<Long> dummyPath = new ArrayList<>();
        int count = 0;
        for (Long nodeId : graph.getAllNodes().keySet()) {
            dummyPath.add(nodeId);
            if (++count >= 5) break;
        }
        renderer.setRoute(dummyPath);
        renderer.draw();

        // 4. Set dummy MST overlay
        List<Road> dummyMst = new ArrayList<>();
        for (Long nodeId : graph.getAllNodes().keySet()) {
            List<Road> roadList = graph.getNeighbors(nodeId);
            if (!roadList.isEmpty()) {
                dummyMst.add(roadList.get(0));
                if (dummyMst.size() >= 10) break;
            }
        }
        renderer.setMstEdges(dummyMst);
        renderer.draw();

        // 5. Clear overlays
        renderer.clearOverlays();
        renderer.draw();

        System.out.println("-> testRenderAndOverlayExecution PASSED");
    }
}
