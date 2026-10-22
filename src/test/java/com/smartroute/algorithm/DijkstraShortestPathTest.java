package com.smartroute.algorithm;

// Explicit imports with purpose comments
import com.smartroute.algorithm.RouteResult.RouteStatus; // Result status enum
import com.smartroute.data.OsmXmlCampusDataLoader;       // XML loader to load test fixtures and real snapshot
import com.smartroute.model.CampusGraph;                 // Graph model
import com.smartroute.model.GraphNode;                   // Vertex model
import com.smartroute.model.Location;                    // Destination model
import com.smartroute.model.Road;                        // Road edge model
import com.smartroute.testutil.SimpleAssert;             // Assertion framework
import java.nio.file.Path;                               // File path utility
import java.util.List;                                   // List interface

/**
 * Unit tests verifying {@link DijkstraShortestPath} implementation correctness,
 * including shortest path optimality, same-location detection, disconnected components,
 * invalid endpoints, and routing over the real VIT Vellore campus graph.
 */
public class DijkstraShortestPathTest {

    public void testDirectVsIndirectShorterPath() {
        CampusGraph graph = new CampusGraph();

        // 3 vertices: 1, 2, 3
        GraphNode n1 = new GraphNode(1L, 12.969, 79.155);
        GraphNode n2 = new GraphNode(2L, 12.970, 79.156);
        GraphNode n3 = new GraphNode(3L, 12.971, 79.157);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);

        // Long direct path: 1 <-> 3 is 100 metres
        graph.addBidirectionalRoad(new Road(1L, 3L, 100.0, 10L, "footway", "Long Avenue"));

        // Shorter indirect path: 1 <-> 2 is 30m, 2 <-> 3 is 40m (Total: 70m)
        graph.addBidirectionalRoad(new Road(1L, 2L, 30.0, 20L, "footway", "Short Lane A"));
        graph.addBidirectionalRoad(new Road(2L, 3L, 40.0, 21L, "footway", "Short Lane B"));

        DijkstraShortestPath router = new DijkstraShortestPath(graph);
        RouteResult result = router.findShortestPath(1L, 3L);

        SimpleAssert.assertTrue(result.isSuccess(), "Route should be found");
        SimpleAssert.assertEquals(RouteStatus.FOUND, result.getStatus(), "Status should be FOUND");
        SimpleAssert.assertEquals(70.0, result.getTotalDistanceMeters(), 1e-6, "Shortest distance must be 70m, not 100m");

        // Verify sequence is [1, 2, 3]
        List<Long> path = result.getPathNodeIds();
        SimpleAssert.assertEquals(3, path.size(), "Path should contain 3 vertices");
        SimpleAssert.assertEquals(1L, path.get(0));
        SimpleAssert.assertEquals(2L, path.get(1));
        SimpleAssert.assertEquals(3L, path.get(2));

        // Verify route legs
        List<RouteLeg> legs = result.getLegs();
        SimpleAssert.assertEquals(2, legs.size(), "Route should have 2 legs");
        SimpleAssert.assertEquals("Short Lane A", legs.get(0).getWayName());
        SimpleAssert.assertEquals(30.0, legs.get(0).getDistanceMeters(), 1e-6);
        SimpleAssert.assertEquals("Short Lane B", legs.get(1).getWayName());
        SimpleAssert.assertEquals(40.0, legs.get(1).getDistanceMeters(), 1e-6);
    }

    public void testSameStartAndDestination() {
        CampusGraph graph = new CampusGraph();
        GraphNode n1 = new GraphNode(1L, 12.969, 79.155);
        graph.addNode(n1);

        DijkstraShortestPath router = new DijkstraShortestPath(graph);
        RouteResult result = router.findShortestPath(1L, 1L);

        SimpleAssert.assertTrue(result.isSuccess(), "Same start and end should succeed");
        SimpleAssert.assertEquals(RouteStatus.SAME_LOCATION, result.getStatus(), "Status should be SAME_LOCATION");
        SimpleAssert.assertEquals(0.0, result.getTotalDistanceMeters(), 1e-6, "Distance must be 0m");
        SimpleAssert.assertEquals(1, result.getPathNodeIds().size());
        SimpleAssert.assertEquals(1L, result.getPathNodeIds().get(0));
        SimpleAssert.assertEquals(0, result.getLegs().size());
    }

    public void testUnreachableDestination() {
        CampusGraph graph = new CampusGraph();

        // Component A: 1 <-> 2
        GraphNode n1 = new GraphNode(1L, 12.969, 79.155);
        GraphNode n2 = new GraphNode(2L, 12.970, 79.156);
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addBidirectionalRoad(new Road(1L, 2L, 50.0));

        // Component B: 3 <-> 4 (completely disconnected from Component A)
        GraphNode n3 = new GraphNode(3L, 12.975, 79.165);
        GraphNode n4 = new GraphNode(4L, 12.976, 79.166);
        graph.addNode(n3);
        graph.addNode(n4);
        graph.addBidirectionalRoad(new Road(3L, 4L, 60.0));

        DijkstraShortestPath router = new DijkstraShortestPath(graph);
        RouteResult result = router.findShortestPath(1L, 3L);

        SimpleAssert.assertFalse(result.isSuccess(), "Disconnected nodes must not yield a path");
        SimpleAssert.assertEquals(RouteStatus.NO_PATH, result.getStatus(), "Status should be NO_PATH");
        SimpleAssert.assertTrue(Double.isInfinite(result.getTotalDistanceMeters()), "Distance should be infinite");
        SimpleAssert.assertEquals(0, result.getPathNodeIds().size());
    }

    public void testInvalidEndpoints() {
        CampusGraph graph = new CampusGraph();
        GraphNode n1 = new GraphNode(1L, 12.969, 79.155);
        graph.addNode(n1);

        DijkstraShortestPath router = new DijkstraShortestPath(graph);

        // Start node missing
        RouteResult res1 = router.findShortestPath(999L, 1L);
        SimpleAssert.assertEquals(RouteStatus.INVALID_START, res1.getStatus());

        // End node missing
        RouteResult res2 = router.findShortestPath(1L, 888L);
        SimpleAssert.assertEquals(RouteStatus.INVALID_DESTINATION, res2.getStatus());
    }

    public void testUnconnectedLocationHandling() {
        CampusGraph graph = new CampusGraph();
        GraphNode n1 = new GraphNode(1L, 12.969, 79.155);
        graph.addNode(n1);

        Location locConnected = new Location("loc1", "Gate", 12.969, 79.155, 1L, Location.SnapType.DIRECT_VERTEX, 0.0);
        Location locUnconnected = new Location("loc2", "Isolated Field", 12.980, 79.170); // unconnected

        graph.addLocation(locConnected);
        graph.addLocation(locUnconnected);

        DijkstraShortestPath router = new DijkstraShortestPath(graph);
        RouteResult result = router.findShortestPath(locConnected, locUnconnected);

        SimpleAssert.assertFalse(result.isSuccess());
        SimpleAssert.assertEquals(RouteStatus.UNCONNECTED_DESTINATION, result.getStatus());
    }

    public void testRealCampusGraphRoute() throws Exception {
        Path xmlPath = Path.of("d:/smart-route-finder-starter/smart-route-finder-starter/src/main/resources/data/vit-vellore-campus.osm.xml");
        OsmXmlCampusDataLoader loader = new OsmXmlCampusDataLoader(35.0);
        CampusGraph graph = loader.load(xmlPath.toFile());

        DijkstraShortestPath router = new DijkstraShortestPath(graph);

        // Pick two adjacent nodes on the network
        // From our validated snapshot:
        List<Location> locations = graph.getAllLocations();
        Location connectedStart = null;
        Location connectedEnd = null;

        for (Location loc : locations) {
            if (loc.isConnected()) {
                if (connectedStart == null) {
                    connectedStart = loc;
                } else if (connectedEnd == null && loc.getConnectedNodeId() != connectedStart.getConnectedNodeId()) {
                    connectedEnd = loc;
                    break;
                }
            }
        }

        SimpleAssert.assertNotNull(connectedStart, "Must have at least one connected destination");
        SimpleAssert.assertNotNull(connectedEnd, "Must have at least two connected destinations");

        RouteResult result = router.findShortestPath(connectedStart, connectedEnd);

        // Even if across disconnected components, result must be non-null and have clean status
        SimpleAssert.assertNotNull(result);
        SimpleAssert.assertTrue(result.getStatus() == RouteStatus.FOUND || result.getStatus() == RouteStatus.NO_PATH);

        if (result.getStatus() == RouteStatus.FOUND) {
            SimpleAssert.assertTrue(result.getTotalDistanceMeters() > 0.0, "Total distance must be positive");
            SimpleAssert.assertTrue(result.getPathNodeIds().size() >= 2, "Path must have at least 2 nodes");
            SimpleAssert.assertTrue(result.getLegs().size() >= 1, "Path must have at least 1 leg");

            // Verify sum of legs equals total distance within floating-point tolerance
            double sumLegs = 0;
            for (RouteLeg leg : result.getLegs()) {
                sumLegs += leg.getDistanceMeters();
            }
            SimpleAssert.assertEquals(result.getTotalDistanceMeters(), sumLegs, 1e-5, "Sum of legs must equal total distance");
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running DijkstraShortestPathTest...");
        DijkstraShortestPathTest test = new DijkstraShortestPathTest();
        test.testDirectVsIndirectShorterPath();
        System.out.println("  * testDirectVsIndirectShorterPath: PASSED");
        test.testSameStartAndDestination();
        System.out.println("  * testSameStartAndDestination: PASSED");
        test.testUnreachableDestination();
        System.out.println("  * testUnreachableDestination: PASSED");
        test.testInvalidEndpoints();
        System.out.println("  * testInvalidEndpoints: PASSED");
        test.testUnconnectedLocationHandling();
        System.out.println("  * testUnconnectedLocationHandling: PASSED");
        test.testRealCampusGraphRoute();
        System.out.println("  * testRealCampusGraphRoute: PASSED");
        System.out.println("All DijkstraShortestPathTest tests PASSED!");
    }
}

