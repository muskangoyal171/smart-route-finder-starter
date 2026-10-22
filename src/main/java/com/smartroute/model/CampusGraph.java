package com.smartroute.model;

// Explicit imports with purpose comments
import java.util.ArrayDeque;       // Used for standard BFS queue during component traversal
import java.util.ArrayList;        // Used for edge lists in the adjacency list
import java.util.Collections;      // Used for unmodifiable views and empty collection constants
import java.util.HashMap;          // Used for fast O(1) average lookup of nodes and adjacency lists
import java.util.HashSet;          // Used for visited node tracking in graph traversal
import java.util.LinkedHashMap;    // Used for preserving insertion order of selectable destinations
import java.util.List;             // Used for adjacency list representation
import java.util.Map;              // Used for graph map interfaces
import java.util.Queue;            // Used for BFS component analysis
import java.util.Set;              // Used for tracking unique vertices

/**
 * Represents the weighted adjacency-list campus graph for VIT Vellore.
 * <p>
 * Vertices are unique OpenStreetMap path nodes (identified by stable 64-bit OSM ID).
 * Edges are physical way segments weighted by their real-world Haversine distance in metres.
 * Named campus destinations are indexed separately and attached to eligible graph vertices.
 * </p>
 * <p>
 * <b>DSA Properties:</b>
 * <ul>
 *   <li>Graph Representation: Adjacency list using {@code Map<Long, List<Road>>}.</li>
 *   <li>Space Complexity: {@code O(V + E)} where {@code V} is the number of vertices and {@code E} is the number of edges.</li>
 *   <li>Lookup Complexity: Average {@code O(1)} for vertex and neighbor retrieval.</li>
 * </ul>
 * </p>
 */
public class CampusGraph {

    /** Earth's mean radius in metres used for geodesic Haversine distance calculations. */
    public static final double EARTH_RADIUS_METERS = 6371000.0;

    private final Map<Long, GraphNode> nodes;
    private final Map<Long, List<Road>> adjacencyList;
    private final Map<String, Location> destinationsById;
    private final Map<String, Location> destinationsByName;

    /**
     * Constructs an empty CampusGraph.
     */
    public CampusGraph() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
        this.destinationsById = new LinkedHashMap<>();
        this.destinationsByName = new LinkedHashMap<>();
    }

    /**
     * Calculates the geodesic surface distance between two geographic coordinates using the Haversine formula.
     *
     * @param lat1 latitude of first point in degrees
     * @param lon1 longitude of first point in degrees
     * @param lat2 latitude of second point in degrees
     * @param lon2 longitude of second point in degrees
     * @return distance in metres
     */
    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double phi1 = Math.toRadians(lat1);
        double phi2 = Math.toRadians(lat2);
        double deltaPhi = Math.toRadians(lat2 - lat1);
        double deltaLambda = Math.toRadians(lon2 - lon1);

        double sinHalfPhi = Math.sin(deltaPhi / 2.0);
        double sinHalfLambda = Math.sin(deltaLambda / 2.0);

        double a = sinHalfPhi * sinHalfPhi +
                Math.cos(phi1) * Math.cos(phi2) * sinHalfLambda * sinHalfLambda;

        // Guard against floating-point inaccuracies pushing 'a' slightly past 1.0
        a = Math.min(1.0, Math.max(0.0, a));
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Adds an OpenStreetMap node vertex to the graph.
     *
     * @param node the GraphNode to add
     */
    public void addNode(GraphNode node) {
        if (node != null) {
            nodes.put(node.getId(), node);
            adjacencyList.putIfAbsent(node.getId(), new ArrayList<>());
        }
    }

    /**
     * Adds a directed road edge from start node to end node.
     *
     * @param road the directed Road edge
     */
    public void addRoad(Road road) {
        if (road == null) return;
        long u = road.getStartNodeId();
        long v = road.getEndNodeId();

        adjacencyList.putIfAbsent(u, new ArrayList<>());
        adjacencyList.putIfAbsent(v, new ArrayList<>());

        adjacencyList.get(u).add(road);
    }

    /**
     * Adds an undirected (bidirectional) road segment by inserting both forward and reverse edges.
     *
     * @param road the forward Road edge
     */
    public void addBidirectionalRoad(Road road) {
        if (road == null) return;
        addRoad(road);
        addRoad(road.createReverse());
    }

    /**
     * Registers a selectable campus destination in the graph.
     *
     * @param location the destination to register
     */
    public void addLocation(Location location) {
        if (location != null) {
            destinationsById.put(location.getId(), location);
            destinationsByName.put(location.getName().toLowerCase(), location);
        }
    }

    public GraphNode getNode(long id) {
        return nodes.get(id);
    }

    public boolean containsNode(long id) {
        return nodes.containsKey(id);
    }

    /**
     * Returns the list of outgoing road edges for a given vertex.
     *
     * @param nodeId the OSM node ID
     * @return unmodifiable list of Road edges, or empty list if vertex has no neighbors
     */
    public List<Road> getNeighbors(long nodeId) {
        List<Road> edges = adjacencyList.get(nodeId);
        if (edges == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(edges);
    }

    public Location getLocationById(String id) {
        if (id == null) return null;
        return destinationsById.get(id);
    }

    public Location getLocationByName(String name) {
        if (name == null) return null;
        return destinationsByName.get(name.trim().toLowerCase());
    }

    /**
     * Searches for registered campus locations whose names contain the given query string (case-insensitive).
     *
     * @param query the partial or full landmark name to search for
     * @return unmodifiable list of matching locations; returns all locations if query is null or blank
     *         Edge cases: Leading/trailing whitespace in query is trimmed. If no matches are found,
     *         returns an empty list.
     */
    public List<Location> searchLocationsByName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllLocations();
        }
        String q = query.trim().toLowerCase();
        List<Location> matches = new ArrayList<>();
        for (Location loc : destinationsById.values()) {
            if (loc.getName().toLowerCase().contains(q)) {
                matches.add(loc);
            }
        }
        return Collections.unmodifiableList(matches);
    }

    public Map<Long, GraphNode> getAllNodes() {
        return Collections.unmodifiableMap(nodes);
    }

    public List<Location> getAllLocations() {
        return Collections.unmodifiableList(new ArrayList<>(destinationsById.values()));
    }

    public int getVertexCount() {
        return nodes.size();
    }

    public int getRoutableVertexCount() {
        int count = 0;
        for (List<Road> edges : adjacencyList.values()) {
            if (!edges.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Counts the total number of directed edges in the adjacency list.
     *
     * @return total directed edge count
     */
    public int getTotalDirectedEdgeCount() {
        int total = 0;
        for (List<Road> edges : adjacencyList.values()) {
            total += edges.size();
        }
        return total;
    }

    /**
     * Performs a Breadth-First Search (BFS) to count connected components across the graph.
     *
     * @return number of disjoint connected components containing at least one edge
     */
    public int countConnectedComponents() {
        Set<Long> visited = new HashSet<>();
        int components = 0;

        for (Map.Entry<Long, List<Road>> entry : adjacencyList.entrySet()) {
            Long startVertex = entry.getKey();
            if (entry.getValue().isEmpty()) continue; // skip isolated zero-degree vertices

            if (!visited.contains(startVertex)) {
                components++;
                Queue<Long> queue = new ArrayDeque<>();
                queue.add(startVertex);
                visited.add(startVertex);

                while (!queue.isEmpty()) {
                    Long current = queue.poll();
                    for (Road edge : getNeighbors(current)) {
                        Long neighbor = edge.getEndNodeId();
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
            }
        }
        return components;
    }

    /**
     * Validates graph invariants. Throws {@link IllegalStateException} if an invariant is violated.
     */
    public void validateInvariants() {
        for (Map.Entry<Long, List<Road>> entry : adjacencyList.entrySet()) {
            long u = entry.getKey();
            if (!nodes.containsKey(u)) {
                throw new IllegalStateException("Adjacency list contains vertex without GraphNode: " + u);
            }
            for (Road road : entry.getValue()) {
                long v = road.getEndNodeId();
                if (!nodes.containsKey(v)) {
                    throw new IllegalStateException("Road edge targets non-existent GraphNode: " + v);
                }
                if (road.getDistanceMeters() < 0.0) {
                    throw new IllegalStateException("Road has negative distance: " + road);
                }
            }
        }
    }
}

