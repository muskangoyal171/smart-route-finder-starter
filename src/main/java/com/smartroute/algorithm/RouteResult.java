package com.smartroute.algorithm;

// Explicit imports with purpose comments
import java.util.Collections; // Used for returning unmodifiable views of path lists
import java.util.List;        // Used for path node sequences and leg lists

/**
 * Encapsulates the complete result of a route search between two campus locations.
 * <p>
 * Contains the execution status, the ordered sequence of graph vertex IDs, individual
 * route legs, total distance in metres, and a user-facing explanation message.
 * </p>
 */
public class RouteResult {

    /**
     * Enumerates the possible outcomes of a route search.
     */
    public enum RouteStatus {
        /** A valid connected walking route was found. */
        FOUND,
        /** Start and destination are the same location (distance is 0 metres). */
        SAME_LOCATION,
        /** No walking path connects the start and destination (disconnected components). */
        NO_PATH,
        /** The selected start location is not recognized in the graph. */
        INVALID_START,
        /** The selected destination location is not recognized in the graph. */
        INVALID_DESTINATION,
        /** The destination cannot be reached because it is not connected to the path network. */
        UNCONNECTED_DESTINATION
    }

    private final RouteStatus status;
    private final List<Long> pathNodeIds;
    private final List<RouteLeg> legs;
    private final double totalDistanceMeters;
    private final String message;

    /**
     * Constructs a RouteResult instance.
     *
     * @param status              the outcome status of the route calculation
     * @param pathNodeIds         ordered list of OSM node IDs along the route
     * @param legs                individual leg segments comprising the route
     * @param totalDistanceMeters sum of all leg distances in metres
     * @param message             descriptive user message
     */
    public RouteResult(RouteStatus status, List<Long> pathNodeIds, List<RouteLeg> legs,
                       double totalDistanceMeters, String message) {
        this.status = status != null ? status : RouteStatus.NO_PATH;
        this.pathNodeIds = pathNodeIds != null ? List.copyOf(pathNodeIds) : Collections.emptyList();
        this.legs = legs != null ? List.copyOf(legs) : Collections.emptyList();
        this.totalDistanceMeters = Math.max(0.0, totalDistanceMeters);
        this.message = message != null ? message : "";
    }

    /**
     * Factory method creating a successful route result.
     */
    public static RouteResult success(List<Long> pathNodeIds, List<RouteLeg> legs, double totalDistanceMeters, String message) {
        return new RouteResult(RouteStatus.FOUND, pathNodeIds, legs, totalDistanceMeters, message);
    }

    /**
     * Factory method for when start and destination are identical.
     */
    public static RouteResult sameLocation(long nodeId, String locationName) {
        String msg = "You are already at " + (locationName != null ? locationName : "this location") + " (Distance: 0.0 m).";
        return new RouteResult(RouteStatus.SAME_LOCATION, List.of(nodeId), Collections.emptyList(), 0.0, msg);
    }

    /**
     * Factory method for when destination is unreachable.
     */
    public static RouteResult noPath(String message) {
        return new RouteResult(RouteStatus.NO_PATH, Collections.emptyList(), Collections.emptyList(), Double.POSITIVE_INFINITY, message);
    }

    /**
     * Factory method for errors (invalid endpoints, unconnected places).
     */
    public static RouteResult error(RouteStatus status, String message) {
        return new RouteResult(status, Collections.emptyList(), Collections.emptyList(), Double.POSITIVE_INFINITY, message);
    }

    public RouteStatus getStatus() {
        return status;
    }

    public boolean isSuccess() {
        return status == RouteStatus.FOUND || status == RouteStatus.SAME_LOCATION;
    }

    public List<Long> getPathNodeIds() {
        return pathNodeIds;
    }

    public List<RouteLeg> getLegs() {
        return legs;
    }

    public double getTotalDistanceMeters() {
        return totalDistanceMeters;
    }

    public String getMessage() {
        return message;
    }

    public int getHopCount() {
        return legs.size();
    }

    @Override
    public String toString() {
        return "RouteResult{" +
                "status=" + status +
                ", distance=" + String.format("%.1f", totalDistanceMeters) + "m" +
                ", hops=" + legs.size() +
                '}';
    }
}

