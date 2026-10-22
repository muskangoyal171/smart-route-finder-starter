package com.smartroute.model;

// Explicit imports with purpose comments
import java.util.Collections; // Used to provide unmodifiable views of node tags
import java.util.HashMap;     // Used to store key-value OSM tags efficiently
import java.util.Map;         // Used for the tag map interface
import java.util.Objects;     // Used for null-safe hash code and equality checks

/**
 * Represents a single point vertex in the OpenStreetMap graph.
 * <p>
 * Each node has a stable 64-bit OSM identifier, geographic latitude and longitude
 * coordinates, and optional descriptive key-value tags (such as name or entrance).
 * </p>
 */
public class GraphNode {

    private final long id;
    private final double latitude;
    private final double longitude;
    private final Map<String, String> tags;

    /**
     * Constructs a GraphNode with coordinates and an optional tag map.
     *
     * @param id        the unique OpenStreetMap node ID
     * @param latitude  geographic latitude in degrees
     * @param longitude geographic longitude in degrees
     * @param tags      optional key-value OSM tags (e.g., name, entrance)
     */
    public GraphNode(long id, double latitude, double longitude, Map<String, String> tags) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        if (tags != null && !tags.isEmpty()) {
            this.tags = new HashMap<>(tags);
        } else {
            this.tags = Collections.emptyMap();
        }
    }

    /**
     * Convenience constructor for a GraphNode without tags.
     *
     * @param id        the unique OpenStreetMap node ID
     * @param latitude  geographic latitude in degrees
     * @param longitude geographic longitude in degrees
     */
    public GraphNode(long id, double latitude, double longitude) {
        this(id, latitude, longitude, null);
    }

    public long getId() {
        return id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Map<String, String> getTags() {
        return Collections.unmodifiableMap(tags);
    }

    public String getTag(String key) {
        return tags.get(key);
    }

    public boolean hasTag(String key) {
        return tags.containsKey(key);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphNode graphNode = (GraphNode) o;
        return id == graphNode.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GraphNode{" +
                "id=" + id +
                ", lat=" + latitude +
                ", lon=" + longitude +
                '}';
    }
}

