package com.smartroute.algorithm;

// Explicit imports with purpose comments
import com.smartroute.data.OsmXmlCampusDataLoader; // XML loader for real snapshot testing
import com.smartroute.model.CampusGraph;           // Graph model
import com.smartroute.model.GraphNode;             // Vertex model
import com.smartroute.model.Road;                  // Road edge model
import com.smartroute.testutil.SimpleAssert;       // Test assertion framework
import java.nio.file.Path;                         // File path utility
import java.util.List;                             // List interface

/**
 * Unit tests verifying Minimum Spanning Tree (MST) algorithms, DisjointSet (DSU),
 * Prim and Kruskal total weight equivalence, and BFS/DFS graph traversals.
 */
public class MstTest {

    public void testDisjointSetOperations() {
        DisjointSet dsu = new DisjointSet();

        dsu.makeSet(1L);
        dsu.makeSet(2L);
        dsu.makeSet(3L);
        dsu.makeSet(4L);

        SimpleAssert.assertEquals(4, dsu.getComponentCount(), "Initial component count must be 4");
        SimpleAssert.assertFalse(dsu.connected(1L, 2L), "1 and 2 should not be connected initially");

        // Merge 1 and 2
        SimpleAssert.assertTrue(dsu.union(1L, 2L), "Union of 1 and 2 should succeed");
        SimpleAssert.assertTrue(dsu.connected(1L, 2L), "1 and 2 should be connected");
        SimpleAssert.assertEquals(3, dsu.getComponentCount(), "Component count should now be 3");

        // Merge 3 and 4
        SimpleAssert.assertTrue(dsu.union(3L, 4L), "Union of 3 and 4 should succeed");
        SimpleAssert.assertFalse(dsu.connected(1L, 3L), "1 and 3 should not be connected yet");

        // Merge {1, 2} and {3, 4}
        SimpleAssert.assertTrue(dsu.union(2L, 3L), "Union of 2 and 3 should merge the components");
        SimpleAssert.assertTrue(dsu.connected(1L, 4L), "1 and 4 should now be connected via transitive sets");
        SimpleAssert.assertEquals(1, dsu.getComponentCount(), "Component count should now be 1");

        // Attempting to merge 1 and 4 must return false (cycle detection)
        SimpleAssert.assertFalse(dsu.union(1L, 4L), "Redundant union must return false to prevent cycles");
    }

    public void testPrimAndKruskalEquivalenceOnConnectedGraph() {
        CampusGraph graph = new CampusGraph();

        // 5 vertices
        for (long i = 1; i <= 5; i++) {
            graph.addNode(new GraphNode(i, 12.969 + i * 0.001, 79.155 + i * 0.001));
        }

        // Weighted edges:
        // 1 - 2 : 10.0m
        // 2 - 3 : 15.0m
        // 1 - 3 : 25.0m (redundant cycle edge with higher weight)
        // 3 - 4 : 30.0m
        // 4 - 5 : 20.0m
        // 2 - 5 : 45.0m (redundant cycle edge)
        graph.addBidirectionalRoad(new Road(1L, 2L, 10.0));
        graph.addBidirectionalRoad(new Road(2L, 3L, 15.0));
        graph.addBidirectionalRoad(new Road(1L, 3L, 25.0));
        graph.addBidirectionalRoad(new Road(3L, 4L, 30.0));
        graph.addBidirectionalRoad(new Road(4L, 5L, 20.0));
        graph.addBidirectionalRoad(new Road(2L, 5L, 45.0));

        // Expected MST edges: (1-2: 10), (2-3: 15), (4-5: 20), (3-4: 30) -> Total weight = 75.0m
        // Exactly V - 1 = 4 edges!
        PrimMinimumSpanningTree prim = new PrimMinimumSpanningTree(graph);
        MstResult primResult = prim.findComponentMst(1L);

        KruskalMinimumSpanningTree kruskal = new KruskalMinimumSpanningTree(graph);
        MstResult kruskalResult = kruskal.findMinimumSpanningTree();

        // 1. Verify edge count is V - 1 = 4
        SimpleAssert.assertEquals(4, primResult.getEdgeCount(), "Prim MST must have exactly V - 1 edges");
        SimpleAssert.assertEquals(4, kruskalResult.getEdgeCount(), "Kruskal MST must have exactly V - 1 edges");

        // 2. Verify total weight equivalence
        SimpleAssert.assertEquals(75.0, primResult.getTotalWeightMeters(), 1e-6, "Prim total weight must be 75.0m");
        SimpleAssert.assertEquals(75.0, kruskalResult.getTotalWeightMeters(), 1e-6, "Kruskal total weight must be 75.0m");
        SimpleAssert.assertEquals(primResult.getTotalWeightMeters(), kruskalResult.getTotalWeightMeters(), 1e-6,
                "Prim and Kruskal total weights must be strictly equal");
    }

    public void testDisconnectedForestHandling() {
        CampusGraph graph = new CampusGraph();

        // Component 1: 3 nodes (1, 2, 3), 2 edges
        graph.addNode(new GraphNode(1L, 12.969, 79.155));
        graph.addNode(new GraphNode(2L, 12.970, 79.156));
        graph.addNode(new GraphNode(3L, 12.971, 79.157));
        graph.addBidirectionalRoad(new Road(1L, 2L, 10.0));
        graph.addBidirectionalRoad(new Road(2L, 3L, 20.0));

        // Component 2: 2 nodes (4, 5), 1 edge
        graph.addNode(new GraphNode(4L, 12.980, 79.170));
        graph.addNode(new GraphNode(5L, 12.981, 79.171));
        graph.addBidirectionalRoad(new Road(4L, 5L, 15.0));

        PrimMinimumSpanningTree prim = new PrimMinimumSpanningTree(graph);
        MstResult primForest = prim.findMinimumSpanningForest();

        KruskalMinimumSpanningTree kruskal = new KruskalMinimumSpanningTree(graph);
        MstResult kruskalForest = kruskal.findMinimumSpanningTree();

        // Total forest edges = (3 - 1) + (2 - 1) = 3 edges
        SimpleAssert.assertEquals(3, primForest.getEdgeCount(), "Prim MSF must have 3 edges");
        SimpleAssert.assertEquals(3, kruskalForest.getEdgeCount(), "Kruskal MSF must have 3 edges");

        // Total forest weight = 10 + 20 + 15 = 45.0m
        SimpleAssert.assertEquals(45.0, primForest.getTotalWeightMeters(), 1e-6);
        SimpleAssert.assertEquals(45.0, kruskalForest.getTotalWeightMeters(), 1e-6);

        SimpleAssert.assertTrue(primForest.isForest(), "Prim must detect forest on disconnected graph");
        SimpleAssert.assertTrue(kruskalForest.isForest(), "Kruskal must detect forest on disconnected graph");
    }

    public void testGraphTraversalBfsAndDfs() {
        CampusGraph graph = new CampusGraph();

        // Connected line: 1 <-> 2 <-> 3
        graph.addNode(new GraphNode(1L, 12.969, 79.155));
        graph.addNode(new GraphNode(2L, 12.970, 79.156));
        graph.addNode(new GraphNode(3L, 12.971, 79.157));
        graph.addBidirectionalRoad(new Road(1L, 2L, 10.0));
        graph.addBidirectionalRoad(new Road(2L, 3L, 15.0));

        // Disconnected node: 4
        graph.addNode(new GraphNode(4L, 12.980, 79.170));

        GraphTraversal traversal = new GraphTraversal(graph);

        List<Long> bfsOrder = traversal.bfs(1L);
        SimpleAssert.assertEquals(3, bfsOrder.size(), "BFS from 1 must visit 3 nodes");
        SimpleAssert.assertEquals(1L, bfsOrder.get(0));

        List<Long> dfsOrder = traversal.dfs(1L);
        SimpleAssert.assertEquals(3, dfsOrder.size(), "DFS from 1 must visit 3 nodes");
        SimpleAssert.assertEquals(1L, dfsOrder.get(0));

        SimpleAssert.assertTrue(traversal.isConnected(1L, 3L), "1 and 3 must be connected");
        SimpleAssert.assertFalse(traversal.isConnected(1L, 4L), "1 and 4 must NOT be connected");
    }

    public void testMstOnRealCampusSnapshot() throws Exception {
        Path xmlPath = Path.of("d:/smart-route-finder-starter/smart-route-finder-starter/src/main/resources/data/vit-vellore-campus.osm.xml");
        OsmXmlCampusDataLoader loader = new OsmXmlCampusDataLoader(35.0);
        CampusGraph graph = loader.load(xmlPath.toFile());

        PrimMinimumSpanningTree prim = new PrimMinimumSpanningTree(graph);
        MstResult primResult = prim.findMinimumSpanningForest();

        KruskalMinimumSpanningTree kruskal = new KruskalMinimumSpanningTree(graph);
        MstResult kruskalResult = kruskal.findMinimumSpanningTree();

        // Both algorithms must find a valid spanning forest
        SimpleAssert.assertTrue(primResult.getEdgeCount() > 1000, "Prim should select > 1000 edges");
        SimpleAssert.assertTrue(kruskalResult.getEdgeCount() > 1000, "Kruskal should select > 1000 edges");

        // For identical graph components, both Prim and Kruskal MUST yield identical total weights!
        SimpleAssert.assertEquals(primResult.getTotalWeightMeters(), kruskalResult.getTotalWeightMeters(), 0.5,
                "Prim and Kruskal total forest weights must be identical on the campus graph");
        SimpleAssert.assertEquals(primResult.getEdgeCount(), kruskalResult.getEdgeCount(),
                "Prim and Kruskal must select the exact same number of edges across components");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running MstTest...");
        MstTest test = new MstTest();
        test.testDisjointSetOperations();
        System.out.println("  * testDisjointSetOperations: PASSED");
        test.testPrimAndKruskalEquivalenceOnConnectedGraph();
        System.out.println("  * testPrimAndKruskalEquivalenceOnConnectedGraph: PASSED");
        test.testDisconnectedForestHandling();
        System.out.println("  * testDisconnectedForestHandling: PASSED");
        test.testGraphTraversalBfsAndDfs();
        System.out.println("  * testGraphTraversalBfsAndDfs: PASSED");
        test.testMstOnRealCampusSnapshot();
        System.out.println("  * testMstOnRealCampusSnapshot: PASSED");
        System.out.println("All MstTest tests PASSED!");
    }
}

