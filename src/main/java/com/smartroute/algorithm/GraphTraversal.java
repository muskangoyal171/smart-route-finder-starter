package com.smartroute.algorithm;

// Explicit imports with purpose comments
import com.smartroute.model.CampusGraph; // Graph model providing nodes and adjacency list
import com.smartroute.model.Road;        // Road edge segment model
import java.util.ArrayDeque;             // Queue for BFS and Stack for iterative DFS
import java.util.ArrayList;              // Ordered list of visited vertices
import java.util.Collections;            // List sorting and unmodifiable utilities
import java.util.HashSet;                // Visited vertex set for O(1) membership checks
import java.util.List;                   // List interface
import java.util.Queue;                  // Queue interface
import java.util.Set;                    // Set interface

/**
 * Diagnostic traversal algorithms providing Breadth-First Search (BFS) and
 * Depth-First Search (DFS) connectivity analysis on the campus graph.
 * <p>
 * <b>DSA Characteristics:</b>
 * <ul>
 *   <li>BFS: Uses a standard FIFO queue ({@link ArrayDeque}) to explore vertices level by level.</li>
 *   <li>DFS: Uses an explicit LIFO stack ({@link ArrayDeque}) to traverse deep along each branch before backtracking.</li>
 *   <li>Time Complexity: {@code O(V + E)} traversing each vertex and edge once.</li>
 *   <li>Space Complexity: {@code O(V)} for visited sets, queue, and stack storage.</li>
 * </ul>
 * </p>
 */
public class GraphTraversal {

    private final CampusGraph graph;

    /**
     * Constructs a GraphTraversal utility with a reference to the campus graph.
     *
     * @param graph the loaded CampusGraph
     */
    public GraphTraversal(CampusGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("CampusGraph cannot be null");
        }
        this.graph = graph;
    }

    /**
     * Executes a Breadth-First Search (BFS) starting from the specified root vertex.
     *
     * @param startNodeId the starting OSM node ID
     * @return ordered list of visited vertices in BFS discovery order
     */
    public List<Long> bfs(long startNodeId) {
        if (!graph.containsNode(startNodeId)) {
            return Collections.emptyList();
        }

        List<Long> order = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();

        visited.add(startNodeId);
        queue.add(startNodeId);

        while (!queue.isEmpty()) {
            Long u = queue.poll();
            order.add(u);

            for (Road edge : graph.getNeighbors(u)) {
                long v = edge.getEndNodeId();
                if (!visited.contains(v)) {
                    visited.add(v);
                    queue.add(v);
                }
            }
        }

        return order;
    }

    /**
     * Executes an iterative Depth-First Search (DFS) starting from the specified root vertex.
     *
     * @param startNodeId the starting OSM node ID
     * @return ordered list of visited vertices in DFS discovery order
     */
    public List<Long> dfs(long startNodeId) {
        if (!graph.containsNode(startNodeId)) {
            return Collections.emptyList();
        }

        List<Long> order = new ArrayList<>();
        Set<Long> visited = new HashSet<>();
        ArrayDeque<Long> stack = new ArrayDeque<>();

        stack.push(startNodeId);

        while (!stack.isEmpty()) {
            Long u = stack.pop();

            if (!visited.contains(u)) {
                visited.add(u);
                order.add(u);

                // Push neighbors onto stack
                for (Road edge : graph.getNeighbors(u)) {
                    long v = edge.getEndNodeId();
                    if (!visited.contains(v)) {
                        stack.push(v);
                    }
                }
            }
        }

        return order;
    }

    /**
     * Checks if a walkable path exists between two graph vertices.
     *
     * @param startNodeId  source vertex ID
     * @param targetNodeId destination vertex ID
     * @return true if target is reachable from start; false otherwise
     */
    public boolean isConnected(long startNodeId, long targetNodeId) {
        if (startNodeId == targetNodeId) return graph.containsNode(startNodeId);
        if (!graph.containsNode(startNodeId) || !graph.containsNode(targetNodeId)) return false;

        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new ArrayDeque<>();

        visited.add(startNodeId);
        queue.add(startNodeId);

        while (!queue.isEmpty()) {
            Long u = queue.poll();
            if (u == targetNodeId) {
                return true;
            }

            for (Road edge : graph.getNeighbors(u)) {
                long v = edge.getEndNodeId();
                if (!visited.contains(v)) {
                    visited.add(v);
                    queue.add(v);
                }
            }
        }

        return false;
    }

    /**
     * Discovers all disjoint component sizes in the graph, sorted descending by size.
     *
     * @return sorted list of component vertex counts
     */
    public List<Integer> findComponentSizes() {
        Set<Long> visited = new HashSet<>();
        List<Integer> sizes = new ArrayList<>();

        for (Long nodeId : graph.getAllNodes().keySet()) {
            if (graph.getNeighbors(nodeId).isEmpty()) {
                continue; // Skip isolated zero-degree vertices
            }

            if (!visited.contains(nodeId)) {
                List<Long> componentNodes = bfs(nodeId);
                visited.addAll(componentNodes);
                sizes.add(componentNodes.size());
            }
        }

        sizes.sort(Collections.reverseOrder());
        return sizes;
    }
}

