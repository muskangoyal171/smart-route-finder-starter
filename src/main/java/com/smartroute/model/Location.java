package com.smartroute.model;

// Explicit imports with purpose comments
import java.util.Objects; // Used for null-safe hash code and equality checks

/**
 * Represents a selectable place or point of interest (POI) on the VIT Vellore campus.
 * <p>
 * A destination may represent a mapped entrance, a named academic building, hostel,
 * food court, library, or gate. If the location does not sit directly on a walkable
 * way, it is attached to the nearest graph vertex within the configured snapping threshold
 * (e.g., 35.0 metres). If no vertex lies within that distance, the location is marked
 * as {@link SnapType#UNCONNECTED}.
 * </p>
 */
public class Location {

    /**
     * Describes how a campus destination was connected to the walkable route graph.
     */
    public enum SnapType {
        /** The location is an explicit graph vertex (e.g., entrance node or road junction). */
        DIRECT_VERTEX,
        /** The location was connected via a mapped entrance node on an adjacent path. */
        MAPPED_ENTRANCE,
        /** The location was snapped to the closest graph vertex within the maximum threshold. */
        PROXIMITY_SNAP,
        /** The location could not be safely snapped and has no route connection. */
        UNCONNECTED
    }

    private final String id;
    private final String name;
    private final double latitude;
    private final double longitude;
    private final long connectedNodeId;
    private final SnapType snapType;
    private final double snapDistanceMeters;

    /**
     * Constructs a Location with all connection details.
     *
     * @param id                 unique identifier for this location
     * @param name               display name (e.g., "EV Periyar Central Library")
     * @param latitude           geographic latitude of the destination centroid/entrance
     * @param longitude          geographic longitude of the destination centroid/entrance
     * @param connectedNodeId    OSM ID of the connected graph vertex, or -1 if unconnected
     * @param snapType           how this destination is attached to the walking graph
     * @param snapDistanceMeters distance in metres to the connected vertex (0.0 if direct)
     */
    public Location(String id, String name, double latitude, double longitude,
                    long connectedNodeId, SnapType snapType, double snapDistanceMeters) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Location ID cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Location name cannot be null or empty");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.latitude = latitude;
        this.longitude = longitude;
        this.connectedNodeId = connectedNodeId;
        this.snapType = snapType != null ? snapType : SnapType.UNCONNECTED;
        this.snapDistanceMeters = Math.max(0.0, snapDistanceMeters);
    }

    /**
     * Convenience constructor for an unconnected location.
     */
    public Location(String id, String name, double latitude, double longitude) {
        this(id, name, latitude, longitude, -1L, SnapType.UNCONNECTED, Double.POSITIVE_INFINITY);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getConnectedNodeId() {
        return connectedNodeId;
    }

    public SnapType getSnapType() {
        return snapType;
    }

    public double getSnapDistanceMeters() {
        return snapDistanceMeters;
    }

    /**
     * Checks whether this location has a valid connection to the walking network.
     *
     * @return true if connected to an eligible graph vertex, false otherwise
     */
    public boolean isConnected() {
        return connectedNodeId != -1L && snapType != SnapType.UNCONNECTED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(id, location.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + (isConnected() ? " [Connected]" : " [Unconnected]");
    }
}

