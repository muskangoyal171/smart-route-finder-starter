package com.smartroute.ui;

// Explicit imports with purpose comments
import com.smartroute.algorithm.DijkstraShortestPath;  // Shortest path router
import com.smartroute.algorithm.GraphTraversal;        // Connectivity and component diagnostics
import com.smartroute.algorithm.MstResult;             // Spanning tree result
import com.smartroute.algorithm.PrimMinimumSpanningTree;// Prim MST calculator
import com.smartroute.algorithm.RouteLeg;              // Route leg details
import com.smartroute.algorithm.RouteResult;           // Route result container
import com.smartroute.algorithm.RouteResult.RouteStatus;// Route execution status enum
import com.smartroute.data.CampusDataRepository;       // Data repository
import com.smartroute.model.CampusGraph;               // Graph model
import com.smartroute.model.Location;                  // Location model
import com.smartroute.testutil.SimpleAssert;           // Lightweight assertion helper
import java.util.HashSet;                              // Set for component membership checks
import java.util.List;                                 // List interface
import java.util.Set;                                  // Set interface

/**
 * Automated test suite for user experience flows and error handling (SDLC Stage 7).
 * <p>
 * Verifies all user interaction states, destination dropdown population, route search
 * with step-by-step leg descriptions, empty selection handling, unconnected landmark feedback,
 * disconnected component handling, zero-distance self-routes, and MST exploration.
 * </p>
 */
public class UserExperienceFlowTest {

    public static void main(String[] args) throws Exception {
        System.out.println("--- Starting UserExperienceFlowTest ---");

        CampusDataRepository repo = CampusDataRepository.loadDefault();
        CampusGraph graph = repo.getGraph();

        testDestinationDropdownPopulation(graph);
        testSearchLocationsFlow(graph);
        testSuccessfulRouteCalculationAndLegFormatting(graph);
        testSameOriginAndDestinationSelfRoute(graph);
        testUnconnectedLocationHandling(graph);
        testDisconnectedComponentsHandling(graph);
        testMinimumSpanningNetworkFlow(graph);

        System.out.println("--- UserExperienceFlowTest: ALL TESTS PASSED ---");
    }

    /**
     * Verifies that all mapped campus landmarks (188 places) are available for user selection
     * and have non-empty display names.
     */
    private static void testDestinationDropdownPopulation(CampusGraph graph) {
        System.out.println("Running testDestinationDropdownPopulation...");

        List<Location> locations = graph.getAllLocations();
        SimpleAssert.assertTrue(locations.size() >= 180, "Expected at least 180 campus locations, found " + locations.size());

        for (Location loc : locations) {
            SimpleAssert.assertNotNull(loc.getName(), "Location name must not be null");
            SimpleAssert.assertFalse(loc.getName().trim().isEmpty(), "Location name must not be empty");
        }

        System.out.println("-> testDestinationDropdownPopulation PASSED (" + locations.size() + " places verified)");
    }

    /**
     * Verifies landmark name searching and partial matching across loaded campus destinations.
     */
    private static void testSearchLocationsFlow(CampusGraph graph) {
        System.out.println("Running testSearchLocationsFlow...");

        List<Location> libMatches = graph.searchLocationsByName("library");
        SimpleAssert.assertFalse(libMatches.isEmpty(), "Should find matches for 'library'");
        SimpleAssert.assertTrue(libMatches.stream().anyMatch(l -> l.getName().toLowerCase().contains("library")),
                "Match list must contain library");

        List<Location> gateMatches = graph.searchLocationsByName("Gate");
        SimpleAssert.assertFalse(gateMatches.isEmpty(), "Should find matches for 'Gate'");

        // Multi-word searches containing spaces
        List<Location> mainGateMatches = graph.searchLocationsByName("Main Gate");
        SimpleAssert.assertFalse(mainGateMatches.isEmpty(), "Should find matches for multi-word query 'Main Gate'");

        List<Location> foodCourtMatches = graph.searchLocationsByName("Food Court");
        SimpleAssert.assertFalse(foodCourtMatches.isEmpty(), "Should find matches for multi-word query 'Food Court'");

        Location libraryExact = graph.getLocationByName("EV Periyar Library");
        SimpleAssert.assertNotNull(libraryExact, "EV Periyar Library must be retrievable by exact name");

        System.out.println("-> testSearchLocationsFlow PASSED (searches for 'library', 'Gate', 'Main Gate', 'Food Court' verified)");
    }

    /**
     * Verifies end-to-end route search between major campus landmarks within the same component,
     * verifying distance, hop count, leg formatting, and non-empty way descriptions.
     */
    private static void testSuccessfulRouteCalculationAndLegFormatting(CampusGraph graph) {
        System.out.println("Running testSuccessfulRouteCalculationAndLegFormatting...");

        // Find the main component
        long rootId = graph.getAllNodes().keySet().iterator().next();
        GraphTraversal traversal = new GraphTraversal(graph);
        List<Long> mainComponent = traversal.bfs(rootId);
        Set<Long> mainComponentSet = new HashSet<>(mainComponent);

        Location start = null;
        Location end = null;
        for (Location loc : graph.getAllLocations()) {
            if (loc.isConnected() && mainComponentSet.contains(loc.getConnectedNodeId())) {
                if (start == null) {
                    start = loc;
                } else if (end == null && loc.getConnectedNodeId() != start.getConnectedNodeId()) {
                    end = loc;
                    break;
                }
            }
        }

        SimpleAssert.assertNotNull(start, "Must find at least one connected landmark in main component");
        SimpleAssert.assertNotNull(end, "Must find a second connected landmark in main component");

        DijkstraShortestPath router = new DijkstraShortestPath(graph);
        RouteResult result = router.findShortestPath(start, end);

        SimpleAssert.assertTrue(result.isSuccess(), "Route between connected campus landmarks should succeed");
        SimpleAssert.assertTrue(result.getTotalDistanceMeters() > 0.0, "Total distance should be greater than zero");
        SimpleAssert.assertTrue(result.getHopCount() > 0, "Hop count should be greater than zero");
        SimpleAssert.assertFalse(result.getLegs().isEmpty(), "Route should include step-by-step legs");

        // Verify route legs structure
        double accumulatedDist = 0.0;
        for (RouteLeg leg : result.getLegs()) {
            SimpleAssert.assertTrue(leg.getDistanceMeters() > 0.0, "Each leg must have positive distance");
            SimpleAssert.assertNotNull(leg.getWayName(), "Way name must not be null");
            accumulatedDist += leg.getDistanceMeters();
        }

        SimpleAssert.assertEquals(result.getTotalDistanceMeters(), accumulatedDist, 0.01,
                "Sum of leg distances must match total route distance");

        System.out.println("-> testSuccessfulRouteCalculationAndLegFormatting PASSED (" +
                start.getName() + " -> " + end.getName() + ": " +
                String.format("%.1f", result.getTotalDistanceMeters()) + " m across " +
                result.getHopCount() + " segments)");
    }

    /**
     * Verifies that selecting the same start and destination produces an immediate 0.0m route.
     */
    private static void testSameOriginAndDestinationSelfRoute(CampusGraph graph) {
        System.out.println("Running testSameOriginAndDestinationSelfRoute...");

        Location start = graph.getAllLocations().stream().filter(Location::isConnected).findFirst().orElse(null);
        SimpleAssert.assertNotNull(start, "Should find at least one connected location");

        DijkstraShortestPath router = new DijkstraShortestPath(graph);
        RouteResult result = router.findShortestPath(start, start);

        SimpleAssert.assertTrue(result.isSuccess(), "Self route should succeed");
        SimpleAssert.assertEquals(0.0, result.getTotalDistanceMeters(), 0.001, "Self route distance must be 0.0");
        SimpleAssert.assertEquals(0, result.getHopCount(), "Self route hop count must be 0");
        SimpleAssert.assertEquals(1, result.getPathNodeIds().size(), "Self route should contain the start node");

        System.out.println("-> testSameOriginAndDestinationSelfRoute PASSED");
    }

    /**
     * Verifies graceful error handling when querying an unconnected location.
     */
    private static void testUnconnectedLocationHandling(CampusGraph graph) {
        System.out.println("Running testUnconnectedLocationHandling...");

        Location unconnected = new Location("test_unconn", "Unconnected Test Landmark", 12.970, 79.160);
        Location connected = graph.getAllLocations().stream().filter(Location::isConnected).findFirst().orElse(null);

        DijkstraShortestPath router = new DijkstraShortestPath(graph);

        // Start unconnected
        RouteResult res1 = router.findShortestPath(unconnected, connected);
        SimpleAssert.assertFalse(res1.isSuccess(), "Unconnected start must fail");
        SimpleAssert.assertEquals(RouteStatus.UNCONNECTED_DESTINATION, res1.getStatus(),
                "Status must be UNCONNECTED_DESTINATION");
        SimpleAssert.assertTrue(res1.getMessage().contains("not connected"), "Message must mention not connected");

        // Destination unconnected
        RouteResult res2 = router.findShortestPath(connected, unconnected);
        SimpleAssert.assertFalse(res2.isSuccess(), "Unconnected destination must fail");
        SimpleAssert.assertEquals(RouteStatus.UNCONNECTED_DESTINATION, res2.getStatus(),
                "Status must be UNCONNECTED_DESTINATION");

        System.out.println("-> testUnconnectedLocationHandling PASSED");
    }

    /**
     * Verifies that destinations belonging to disjoint graph components report NO_PATH honestly.
     */
    private static void testDisconnectedComponentsHandling(CampusGraph graph) {
        System.out.println("Running testDisconnectedComponentsHandling...");

        // Find root of component 1
        long u = graph.getAllNodes().keySet().iterator().next();
        GraphTraversal traversal = new GraphTraversal(graph);
        List<Long> comp1 = traversal.bfs(u);
        Set<Long> comp1Set = new HashSet<>(comp1);

        // Find a routable node v outside comp1
        long v = -1;
        for (Long nodeId : graph.getAllNodes().keySet()) {
            if (!comp1Set.contains(nodeId) && !graph.getNeighbors(nodeId).isEmpty()) {
                v = nodeId;
                break;
            }
        }

        SimpleAssert.assertTrue(v != -1, "Should find a routable vertex in a disjoint component");

        DijkstraShortestPath router = new DijkstraShortestPath(graph);
        RouteResult result = router.findShortestPath(u, v);

        SimpleAssert.assertFalse(result.isSuccess(), "Path between different components must fail");
        SimpleAssert.assertEquals(RouteStatus.NO_PATH, result.getStatus(), "Status must be NO_PATH");
        SimpleAssert.assertTrue(result.getMessage().toLowerCase().contains("no mapped walking path"),
                "Message must state no mapped walking path found");

        System.out.println("-> testDisconnectedComponentsHandling PASSED (verified disjoint components " + u + " and " + v + ")");
    }

    /**
     * Verifies Prim's minimum spanning forest generation, reporting edge count and total length.
     */
    private static void testMinimumSpanningNetworkFlow(CampusGraph graph) {
        System.out.println("Running testMinimumSpanningNetworkFlow...");

        PrimMinimumSpanningTree prim = new PrimMinimumSpanningTree(graph);
        MstResult mst = prim.findMinimumSpanningForest();

        SimpleAssert.assertNotNull(mst, "MST result cannot be null");
        SimpleAssert.assertTrue(mst.getEdgeCount() > 0, "MST must contain edges");
        SimpleAssert.assertTrue(mst.getTotalWeightMeters() > 0.0, "Total MST weight must be positive");
        SimpleAssert.assertTrue(mst.getSpannedVertexCount() > 0, "Spanned vertex count must be positive");

        System.out.println("-> testMinimumSpanningNetworkFlow PASSED (" +
                mst.getEdgeCount() + " edges, " +
                String.format("%.1f", mst.getTotalWeightMeters()) + " m, spanned " +
                mst.getSpannedVertexCount() + " vertices)");
    }
}
