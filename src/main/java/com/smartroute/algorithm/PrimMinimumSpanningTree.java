package com.smartroute.algorithm;

// Explicit imports with purpose comments
import com.smartroute.model.CampusGraph;   // Graph model containing vertices and adjacency list
import com.smartroute.model.Road;          // Road edge segment model
import java.util.ArrayList;                // Dynamic list for selected tree edges
import java.util.Comparator;               // Comparator for ordering edges in the min-priority queue
import java.util.HashSet;                  // Fast O(1) set membership check for vertices in the MST
import java.util.List;                     // List interface
import java.util.PriorityQueue;            // Binary min-heap for lowest-weight connecting edge selection
import java.util.Set;                      // Set interface

/**
 * Implements Prim's algorithm to calculate a Minimum Spanning Tree (MST) on the campus graph.
 * <p>
 * <b>DSA Characteristics:</b>
 * <ul>
 *   <li>Approach: Greedy algorithm growing a tree from an initial root vertex.</li>
 *   <li>Data Structure: Binary min-heap ({@link PriorityQueue}) storing crossing edges ordered by weight.</li>
 *   <li>Time Complexity: {@code O((V + E) log V)} using adjacency list and binary heap.</li>
 *   <li>Space Complexity: {@code O(V + E)} for visited sets and priority queue elements.</li>
 *   <li>Invariant: An MST spanning {@code V_c} connected vertices contains exactly {@code V_c - 1} edges.</li>
 * </ul>
 * </p>
 */
public class PrimMinimumSpanningTree {

    private final CampusGraph graph;

    /**
     * Constructs a PrimMinimumSpanningTree solver with a reference to the campus graph.
     *
     * @param graph the loaded CampusGraph
     */
    public PrimMinimumSpanningTree(CampusGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("CampusGraph cannot be null");
        }
        this.graph = graph;
    }

    /**
     * Calculates the Minimum Spanning Tree for the connected component containing the specified root vertex.
     *
     * @param rootNodeId the vertex from which to begin growing the tree
     * @return the MstResult containing selected edges and total weight
     */
    public MstResult findComponentMst(long rootNodeId) {
        if (!graph.containsNode(rootNodeId)) {
            return new MstResult("Prim", List.of(), 0.0, 0, false);
        }

        Set<Long> inMst = new HashSet<>();
        List<Road> selectedEdges = new ArrayList<>();
        PriorityQueue<Road> pq = new PriorityQueue<>(Comparator.comparingDouble(Road::getDistanceMeters));
        double totalWeight = 0.0;

        // Initialize with root vertex
        inMst.add(rootNodeId);
        for (Road edge : graph.getNeighbors(rootNodeId)) {
            pq.add(edge);
        }

        while (!pq.isEmpty()) {
            Road minEdge = pq.poll();
            long v = minEdge.getEndNodeId();

            // Skip if destination vertex is already in the tree (avoids cycle)
            if (inMst.contains(v)) {
                continue;
            }

            // Greedily include vertex and edge in the MST
            inMst.add(v);
            selectedEdges.add(minEdge);
            totalWeight += minEdge.getDistanceMeters();

            // Push all incident crossing edges into the min-heap
            for (Road edge : graph.getNeighbors(v)) {
                if (!inMst.contains(edge.getEndNodeId())) {
                    pq.add(edge);
                }
            }
        }

        return new MstResult("Prim", selectedEdges, totalWeight, inMst.size(), false);
    }

    /**
     * Calculates the Minimum Spanning Forest (MSF) across all connected components in the campus graph.
     *
     * @return the MstResult encompassing all disjoint component spanning trees
     */
    public MstResult findMinimumSpanningForest() {
        Set<Long> visitedVertices = new HashSet<>();
        List<Road> allTreeEdges = new ArrayList<>();
        double totalWeight = 0.0;
        int componentCount = 0;

        for (Long nodeId : graph.getAllNodes().keySet()) {
            // Only process nodes with edges that have not yet been spanned
            if (visitedVertices.contains(nodeId) || graph.getNeighbors(nodeId).isEmpty()) {
                continue;
            }

            componentCount++;
            MstResult compResult = findComponentMst(nodeId);

            for (Road edge : compResult.getEdges()) {
                allTreeEdges.add(edge);
                visitedVertices.add(edge.getStartNodeId());
                visitedVertices.add(edge.getEndNodeId());
            }
            visitedVertices.add(nodeId);
            totalWeight += compResult.getTotalWeightMeters();
        }

        boolean isForest = componentCount > 1;
        return new MstResult("Prim", allTreeEdges, totalWeight, visitedVertices.size(), isForest);
    }
}

