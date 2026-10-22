package com.smartroute.algorithm;

// Explicit imports with purpose comments
import com.smartroute.algorithm.RouteResult.RouteStatus; // Result status enum
import com.smartroute.model.CampusGraph;                 // Campus graph containing adjacency list and nodes
import com.smartroute.model.Location;                    // Location destination model
import com.smartroute.model.Road;                        // Road edge model
import java.util.ArrayList;                              // Ordered lists for reconstructed path and legs
import java.util.Collections;                            // List reversal and empty collection utilities
import java.util.Comparator;                             // Comparator for priority queue ordering
import java.util.HashMap;                                // Map for distances, parent vertices, and edges
import java.util.HashSet;                                // Visited vertex tracking set
import java.util.List;                                   // Path lists interface
import java.util.Map;                                    // Map interfaces
import java.util.PriorityQueue;                          // Min-heap for closest unvisited vertex selection
import java.util.Set;                                    // Set interfaces

/**
 * Implements Dijkstra's algorithm to calculate the shortest mapped walking route on the campus graph.
 * <p>
 * <b>DSA Characteristics:</b>
 * <ul>
 *   <li>Data Structure: Uses a binary min-heap ({@link PriorityQueue}) paired with an adjacency list.</li>
 *   <li>Time Complexity: {@code O((V + E) log V)} where {@code V} is the number of vertices and {@code E} is the number of edges.</li>
 *   <li>Space Complexity: {@code O(V)} auxiliary space for distance, predecessor, and visited sets.</li>
 *   <li>Preconditions: All edge weights must be non-negative ({@code w(e) >= 0}).</li>
 * </ul>
 * </p>
 */
public class DijkstraShortestPath {

    private final CampusGraph graph;

    /**
     * Constructs the router with a reference to the active campus graph.
     *
     * @param graph the loaded CampusGraph
     */
    public DijkstraShortestPath(CampusGraph graph) {
        if (graph == null) {
            throw new IllegalArgumentException("CampusGraph cannot be null");
        }
        this.graph = graph;
    }

    /**
     * Calculates the shortest path between two selectable campus {@link Location} objects.
     *
     * @param startLocation the starting location
     * @param endLocation   the destination location
     * @return the RouteResult containing the path, distance, and status
     */
    public RouteResult findShortestPath(Location startLocation, Location endLocation) {
        if (startLocation == null) {
            return RouteResult.error(RouteStatus.INVALID_START, "Start location was not specified.");
        }
        if (endLocation == null) {
            return RouteResult.error(RouteStatus.INVALID_DESTINATION, "Destination location was not specified.");
        }

        // Check for identical location
        if (startLocation.equals(endLocation)) {
            return RouteResult.sameLocation(startLocation.getConnectedNodeId(), startLocation.getName());
        }

        // Check if either destination is unconnected
        if (!startLocation.isConnected()) {
            return RouteResult.error(RouteStatus.UNCONNECTED_DESTINATION,
                    "Starting place '" + startLocation.getName() + "' is not connected to a mapped walking path.");
        }
        if (!endLocation.isConnected()) {
            return RouteResult.error(RouteStatus.UNCONNECTED_DESTINATION,
                    "Destination '" + endLocation.getName() + "' is not connected to a mapped walking path.");
        }

        return findShortestPath(startLocation.getConnectedNodeId(), endLocation.getConnectedNodeId());
    }

    /**
     * Calculates the shortest path between two campus locations by their display names.
     *
     * @param startName the name of the start location
     * @param endName   the name of the destination location
     * @return the RouteResult
     */
    public RouteResult findShortestPath(String startName, String endName) {
        if (startName == null || startName.trim().isEmpty()) {
            return RouteResult.error(RouteStatus.INVALID_START, "Start location name is empty.");
        }
        if (endName == null || endName.trim().isEmpty()) {
            return RouteResult.error(RouteStatus.INVALID_DESTINATION, "Destination location name is empty.");
        }

        Location startLoc = graph.getLocationByName(startName);
        if (startLoc == null) {
            return RouteResult.error(RouteStatus.INVALID_START, "Location '" + startName + "' was not found.");
        }

        Location endLoc = graph.getLocationByName(endName);
        if (endLoc == null) {
            return RouteResult.error(RouteStatus.INVALID_DESTINATION, "Location '" + endName + "' was not found.");
        }

        return findShortestPath(startLoc, endLoc);
    }

    /**
     * Calculates the shortest path between two graph vertices by their OpenStreetMap node IDs.
     *
     * @param startNodeId the OSM ID of the start vertex
     * @param endNodeId   the OSM ID of the destination vertex
     * @return the RouteResult
     */
    public RouteResult findShortestPath(long startNodeId, long endNodeId) {
        if (startNodeId == endNodeId) {
            return RouteResult.sameLocation(startNodeId, "Node " + startNodeId);
        }

        if (!graph.containsNode(startNodeId)) {
            return RouteResult.error(RouteStatus.INVALID_START, "Start node ID " + startNodeId + " does not exist in graph.");
        }
        if (!graph.containsNode(endNodeId)) {
            return RouteResult.error(RouteStatus.INVALID_DESTINATION, "Destination node ID " + endNodeId + " does not exist in graph.");
        }

        // Tentative distance from startNodeId to each vertex
        Map<Long, Double> dist = new HashMap<>();
        // Predecessor vertex pointers for path reconstruction
        Map<Long, Long> parent = new HashMap<>();
        // Predecessor edge pointers for leg distance and name attribution
        Map<Long, Road> parentEdge = new HashMap<>();
        // Set of finalized (settled) vertices
        Set<Long> visited = new HashSet<>();

        // Priority queue ordered by tentative distance (min-heap)
        PriorityQueue<NodeDistance> pq = new PriorityQueue<>(Comparator.comparingDouble(NodeDistance::distance));

        dist.put(startNodeId, 0.0);
        pq.add(new NodeDistance(startNodeId, 0.0));

        while (!pq.isEmpty()) {
            NodeDistance current = pq.poll();
            long u = current.nodeId;

            // Early exit: destination has been finalized with minimum distance
            if (u == endNodeId) {
                break;
            }

            // Skip outdated heap entries
            if (visited.contains(u)) {
                continue;
            }
            visited.add(u);

            double distU = current.distance;

            for (Road edge : graph.getNeighbors(u)) {
                long v = edge.getEndNodeId();
                if (visited.contains(v)) {
                    continue;
                }

                double weight = edge.getDistanceMeters();
                double newDist = distU + weight;

                if (newDist < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, newDist);
                    parent.put(v, u);
                    parentEdge.put(v, edge);
                    pq.add(new NodeDistance(v, newDist));
                }
            }
        }

        // Verify reachability
        if (!dist.containsKey(endNodeId) || dist.get(endNodeId) == Double.POSITIVE_INFINITY) {
            return RouteResult.noPath("No mapped walking path connects node " + startNodeId + " to node " + endNodeId + ".");
        }

        // Reconstruct path from destination backward to source
        List<Long> pathNodes = new ArrayList<>();
        List<RouteLeg> legs = new ArrayList<>();

        long curr = endNodeId;
        while (curr != startNodeId) {
            pathNodes.add(curr);
            Road edge = parentEdge.get(curr);
            if (edge != null) {
                legs.add(new RouteLeg(edge.getStartNodeId(), edge.getEndNodeId(), edge.getDistanceMeters(), edge.getWayName()));
            }
            curr = parent.get(curr);
        }
        pathNodes.add(startNodeId);

        // Reverse to obtain source-to-destination order
        Collections.reverse(pathNodes);
        Collections.reverse(legs);

        double totalDistance = dist.get(endNodeId);
        String msg = String.format("Shortest route: %.1f m across %d walking segments.", totalDistance, legs.size());

        return RouteResult.success(pathNodes, legs, totalDistance, msg);
    }

    /**
     * Helper record holding a vertex ID and its tentative distance in the min-priority queue.
     */
    private static record NodeDistance(long nodeId, double distance) {}
}

