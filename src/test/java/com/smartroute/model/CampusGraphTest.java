package com.smartroute.model;

// Explicit imports with purpose comments
import com.smartroute.testutil.SimpleAssert; // Lightweight assertion utility
import java.util.List;                       // Used for checking neighbor edge lists

/**
 * Unit tests verifying {@link CampusGraph} data structure invariants, Haversine formula
 * calculations, adjacency list behavior, and BFS connected component diagnostics.
 */
public class CampusGraphTest {

    public void testHaversineDistanceKnownPoints() {
        // Known coordinates in Vellore:
        // Point A: Main Gate (12.9692, 79.1559)
        // Point B: Silver Jubilee Tower (12.9716, 79.1594)
        double distance = CampusGraph.calculateHaversineDistance(12.9692, 79.1559, 12.9716, 79.1594);

        // Geodesic distance should be approximately 465 metres (+/- 15m)
        SimpleAssert.assertTrue(distance > 450.0 && distance < 480.0,
                "Distance should be ~465m, but got: " + distance);

        // Zero distance for identical points
        double zeroDist = CampusGraph.calculateHaversineDistance(12.9692, 79.1559, 12.9692, 79.1559);
        SimpleAssert.assertEquals(0.0, zeroDist, 1e-6, "Distance between identical points must be zero");
    }

    public void testGraphConstructionAndNeighbors() {
        CampusGraph graph = new CampusGraph();

        GraphNode n1 = new GraphNode(101L, 12.969, 79.155);
        GraphNode n2 = new GraphNode(102L, 12.970, 79.156);
        GraphNode n3 = new GraphNode(103L, 12.971, 79.157);

        graph.addNode(n1);
        graph.addNode(n2);
        graph.addNode(n3);

        SimpleAssert.assertEquals(3, graph.getVertexCount(), "Vertex count must be 3");
        SimpleAssert.assertTrue(graph.containsNode(101L), "Graph must contain node 101");

        Road r1 = new Road(101L, 102L, 150.0, 1L, "footway", "Path 1");
        Road r2 = new Road(102L, 103L, 200.0, 2L, "footway", "Path 2");

        graph.addBidirectionalRoad(r1);
        graph.addBidirectionalRoad(r2);

        List<Road> n1Edges = graph.getNeighbors(101L);
        SimpleAssert.assertEquals(1, n1Edges.size(), "Node 101 should have 1 neighbor");
        SimpleAssert.assertEquals(102L, n1Edges.get(0).getEndNodeId(), "Neighbor of 101 should be 102");
        SimpleAssert.assertEquals(150.0, n1Edges.get(0).getDistanceMeters(), 1e-6, "Edge distance should be 150m");

        List<Road> n2Edges = graph.getNeighbors(102L);
        SimpleAssert.assertEquals(2, n2Edges.size(), "Node 102 should have 2 neighbors");

        SimpleAssert.assertEquals(4, graph.getTotalDirectedEdgeCount(), "Total directed edges should be 4");
        SimpleAssert.assertEquals(1, graph.countConnectedComponents(), "Graph should have 1 component");
    }

    public void testDisconnectedComponentCounting() {
        CampusGraph graph = new CampusGraph();

        // Component 1: 101 <-> 102
        GraphNode n1 = new GraphNode(101L, 12.969, 79.155);
        GraphNode n2 = new GraphNode(102L, 12.970, 79.156);
        graph.addNode(n1);
        graph.addNode(n2);
        graph.addBidirectionalRoad(new Road(101L, 102L, 100.0));

        // Component 2: 201 <-> 202
        GraphNode n3 = new GraphNode(201L, 12.975, 79.160);
        GraphNode n4 = new GraphNode(202L, 12.976, 79.161);
        graph.addNode(n3);
        graph.addNode(n4);
        graph.addBidirectionalRoad(new Road(201L, 202L, 120.0));

        SimpleAssert.assertEquals(2, graph.countConnectedComponents(), "Should have 2 disjoint components");
    }

    public void testNegativeDistanceRejection() {
        SimpleAssert.assertThrows(IllegalArgumentException.class, () -> {
            new Road(101L, 102L, -50.0);
        }, "Road constructor must reject negative weights");
    }

    public void testInvariantValidation() {
        CampusGraph graph = new CampusGraph();
        GraphNode n1 = new GraphNode(101L, 12.969, 79.155);
        graph.addNode(n1);

        // Road to non-existent node 999L
        graph.addRoad(new Road(101L, 999L, 50.0));

        SimpleAssert.assertThrows(IllegalStateException.class, graph::validateInvariants,
                "validateInvariants must throw when edge references missing node");
    }

    public void testLocationSearch() {
        CampusGraph graph = new CampusGraph();
        Location loc1 = new Location("loc1", "EV Periyar Library", 12.969, 79.155);
        Location loc2 = new Location("loc2", "Main Gate", 12.968, 79.154);
        Location loc3 = new Location("loc3", "Food Court 1", 12.970, 79.156);
        graph.addLocation(loc1);
        graph.addLocation(loc2);
        graph.addLocation(loc3);

        List<Location> libResults = graph.searchLocationsByName("library");
        SimpleAssert.assertEquals(1, libResults.size(), "Should find 1 match for 'library'");
        SimpleAssert.assertEquals("EV Periyar Library", libResults.get(0).getName(), "Match should be EV Periyar Library");

        List<Location> allResults = graph.searchLocationsByName("");
        SimpleAssert.assertEquals(3, allResults.size(), "Empty query should return all 3 locations");

        List<Location> noResults = graph.searchLocationsByName("NonExistentPlace");
        SimpleAssert.assertEquals(0, noResults.size(), "Non-matching query should return empty list");
    }

    public static void main(String[] args) {
        System.out.println("Running CampusGraphTest...");
        CampusGraphTest test = new CampusGraphTest();
        test.testHaversineDistanceKnownPoints();
        test.testGraphConstructionAndNeighbors();
        test.testDisconnectedComponentCounting();
        test.testNegativeDistanceRejection();
        test.testInvariantValidation();
        test.testLocationSearch();
        System.out.println("All CampusGraphTest tests PASSED!");
    }
}

