package com.smartroute.algorithm;

// Explicit imports with purpose comments
import java.util.Objects; // Used for null-safe hash code and equality checks

/**
 * Represents a single directed leg (hop) along a calculated walking route.
 * <p>
 * Each leg records the source vertex ID, target vertex ID, segment distance in metres,
 * and optional path/street name (e.g., "Fleming Road").
 * </p>
 */
public class RouteLeg {

    private final long startNodeId;
    private final long endNodeId;
    private final double distanceMeters;
    private final String wayName;

    /**
     * Constructs a RouteLeg instance.
     *
     * @param startNodeId    OSM ID of the starting vertex
     * @param endNodeId      OSM ID of the ending vertex
     * @param distanceMeters ground distance in metres for this leg
     * @param wayName        optional street or walkway name
     */
    public RouteLeg(long startNodeId, long endNodeId, double distanceMeters, String wayName) {
        if (distanceMeters < 0.0) {
            throw new IllegalArgumentException("Leg distance cannot be negative: " + distanceMeters);
        }
        this.startNodeId = startNodeId;
        this.endNodeId = endNodeId;
        this.distanceMeters = distanceMeters;
        this.wayName = wayName != null ? wayName : "Walkway";
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

    public String getWayName() {
        return wayName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteLeg routeLeg = (RouteLeg) o;
        return startNodeId == routeLeg.startNodeId &&
                endNodeId == routeLeg.endNodeId &&
                Double.compare(routeLeg.distanceMeters, distanceMeters) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startNodeId, endNodeId, distanceMeters);
    }

    @Override
    public String toString() {
        return startNodeId + " -> " + endNodeId + " (" + String.format("%.1f", distanceMeters) + "m on " + wayName + ")";
    }
}

