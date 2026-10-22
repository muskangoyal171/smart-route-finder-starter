package com.smartroute.model;

// Explicit imports with purpose comments
import java.util.Objects; // Used for null-safe hash code and equality checks

/**
 * Represents a single directed edge in the campus graph between two consecutive OSM nodes.
 * <p>
 * For bidirectional pedestrian paths, two {@code Road} edges are added to the adjacency list:
 * one from node {@code u} to node {@code v}, and one from node {@code v} to node {@code u}.
 * The weight represents the real physical ground distance in metres calculated using the
 * geodesic Haversine formula.
 * </p>
 */
public class Road {

    private final long startNodeId;
    private final long endNodeId;
    private final double distanceMeters;
    private final long wayId;
    private final String highwayType;
    private final String wayName;

    /**
     * Constructs a Road segment between two nodes.
     *
     * @param startNodeId    OSM ID of the source node
     * @param endNodeId      OSM ID of the target node
     * @param distanceMeters segment length in metres (must be non-negative)
     * @param wayId          OSM ID of the parent way
     * @param highwayType    the highway tag value (e.g., footway, residential)
     * @param wayName        optional road or path name (e.g., "Fleming Road")
     */
    public Road(long startNodeId, long endNodeId, double distanceMeters, long wayId, String highwayType, String wayName) {
        if (distanceMeters < 0.0) {
            throw new IllegalArgumentException("Road distance cannot be negative: " + distanceMeters);
        }
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.distanceMeters = distanceMeters;
        this.wayId = wayId;
        this.highwayType = highwayType != null ? highwayType : "unspecified";
        this.wayName = wayName;
    }

    /**
     * Convenience constructor with minimal fields.
     *
     * @param startNodeId    OSM ID of the source node
     * @param endNodeId      OSM ID of the target node
     * @param distanceMeters segment length in metres
     */
    public Road(long startNodeId, long endNodeId, double distanceMeters) {
        this(startNodeId, endNodeId, distanceMeters, 0L, "footway", null);
    }

    public long getStartNodeId() {
        return startNodeId;
    }

    public long getEndNodeId() {
        return endNodeId;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public long getWayId() {
        return wayId;
    }

    public String getHighwayType() {
        return highwayType;
    }

    public String getWayName() {
        return wayName;
    }

    /**
     * Returns a reversed directed edge (from endNodeId to startNodeId) with identical attributes.
     *
     * @return a new reversed Road instance
     */
    public Road createReverse() {
        return new Road(endNodeId, startNodeId, distanceMeters, wayId, highwayType, wayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Road road = (Road) o;
        return startNodeId == road.startNodeId &&
                endNodeId == road.endNodeId &&
                wayId == road.wayId &&
                Double.compare(road.distanceMeters, distanceMeters) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startNodeId, endNodeId, wayId, distanceMeters);
    }

    @Override
    public String toString() {
        return "Road{" +
                startNodeId + " -> " + endNodeId +
                ", " + String.format("%.1f", distanceMeters) + "m" +
                (wayName != null ? " (" + wayName + ")" : "") +
                '}';
    }
}

