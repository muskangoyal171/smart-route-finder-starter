package com.smartroute.algorithm;

// Explicit imports with purpose comments
import com.smartroute.model.CampusGraph; // Graph model containing vertices and edges
import com.smartroute.model.Road;        // Road edge segment model
import java.util.ArrayList;              // Dynamic list for candidate and selected edges
import java.util.Comparator;             // Comparator for edge sorting by weight
import java.util.HashSet;                // Set for deduplicating undirected edges and tracking spanned vertices
import java.util.List;                   // List interface
import java.util.Set;                    // Set interface

/**
 * Implements Kruskal's algorithm to calculate a Minimum Spanning Tree (MST) on the campus graph.
 * <p>
 * <b>DSA Characteristics:</b>
 * <ul>
 *   <li>Approach: Greedy edge-centric algorithm selecting lowest-weight edges that do not form a cycle.</li>
 *   <li>Data Structure: Uses {@link DisjointSet} (Union-Find with path compression and union-by-rank) for cycle detection.</li>
 *   <li>Time Complexity: {@code O(E log E)} dominated by sorting {@code E} edges.</li>
 *   <li>Space Complexity: {@code O(V + E)} for storing edge lists and DisjointSet structures.</li>
 *   <li>Equivalence: On any connected component, Kruskal produces the exact same total minimum weight as Prim.</li>
 * </ul>
 * </p>
 */
public class KruskalMinimumSpanningTree {

    private final CampusGraph graph;

    /**
     * Constructs a KruskalMinimumSpanningTree solver with a reference to the campus graph.
     *
     * @param graph the loaded CampusGraph
     */
    public KruskalMinimumSpanningTree(CampusGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("CampusGraph cannot be null");
        }
        this.graph = graph;
    }

    /**
     * Calculates the Minimum Spanning Forest across all components (or MST if connected).
     *
     * @return the MstResult containing selected edges and total weight
     */
    public MstResult findMinimumSpanningTree() {
        // 1. Extract and deduplicate undirected edges
        List<Road> uniqueEdges = new ArrayList<>();
        Set<String> seenEdges = new HashSet<>();

        for (Long u : graph.getAllNodes().keySet()) {
            for (Road edge : graph.getNeighbors(u)) {
                long v = edge.getEndNodeId();
                // Create a canonical edge key (minId_maxId) to ensure each bidirectional segment is examined once
                long minId = Math.min(u, v);
                long maxId = Math.max(u, v);
                String edgeKey = minId + "_" + maxId + "_" + String.format("%.3f", edge.getDistanceMeters());

                if (!seenEdges.contains(edgeKey)) {
                    seenEdges.add(edgeKey);
                    uniqueEdges.add(edge);
                }
            }
        }

        // 2. Sort all unique edges in non-decreasing order of weight: O(E log E)
        uniqueEdges.sort(Comparator.comparingDouble(Road::getDistanceMeters));

        // 3. Initialize Disjoint Set Union with all graph vertices
        DisjointSet dsu = new DisjointSet();
        for (Long nodeId : graph.getAllNodes().keySet()) {
            dsu.makeSet(nodeId);
        }

        List<Road> selectedEdges = new ArrayList<>();
        Set<Long> spannedVertices = new HashSet<>();
        double totalWeight = 0.0;

        // 4. Greedily select edges that connect disjoint components (no cycles)
        for (Road edge : uniqueEdges) {
            long u = edge.getStartNodeId();
            long v = edge.getEndNodeId();

            if (dsu.union(u, v)) {
                selectedEdges.add(edge);
                spannedVertices.add(u);
                spannedVertices.add(v);
                totalWeight += edge.getDistanceMeters();
            }
        }

        int components = graph.countConnectedComponents();
        boolean isForest = components > 1;

        return new MstResult("Kruskal", selectedEdges, totalWeight, spannedVertices.size(), isForest);
    }
}

