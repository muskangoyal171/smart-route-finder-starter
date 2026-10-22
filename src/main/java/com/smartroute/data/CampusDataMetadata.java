package com.smartroute.data;

// Explicit imports with purpose comments
import java.io.IOException;     // Used for handling property loading I/O exceptions
import java.io.InputStream;     // Used for reading the properties resource stream
import java.util.Properties;    // Used for standard Java key-value property file loading

/**
 * Encapsulates dataset provenance, geographic boundaries, licensing, and filter parameters
 * loaded from {@code campus-data-metadata.properties}.
 */
public class CampusDataMetadata {

    private final String sourceName;
    private final String sourceUrl;
    private final String exportDate;
    private final double bboxSouth;
    private final double bboxWest;
    private final double bboxNorth;
    private final double bboxEast;
    private final String licenceName;
    private final String attributionText;
    private final String attributionUrl;
    private final double maxSnapDistanceMeters;
    private final String knownLimitations;

    /**
     * Constructs a CampusDataMetadata instance from a loaded Properties object.
     *
     * @param props the loaded properties
     */
    public CampusDataMetadata(Properties props) {
        if (props == null) {
            props = new Properties();
        }
        this.sourceName = props.getProperty("source.name", "OpenStreetMap");
        this.sourceUrl = props.getProperty("source.url", "https://www.openstreetmap.org");
        this.exportDate = props.getProperty("source.export.date", "unknown");

        this.bboxSouth = parseDouble(props.getProperty("bbox.south"), 12.9640);
        this.bboxWest = parseDouble(props.getProperty("bbox.west"), 79.1500);
        this.bboxNorth = parseDouble(props.getProperty("bbox.north"), 12.9790);
        this.bboxEast = parseDouble(props.getProperty("bbox.east"), 79.1670);

        this.licenceName = props.getProperty("licence.name", "ODbL 1.0");
        this.attributionText = props.getProperty("attribution.text", "Map data © OpenStreetMap contributors");
        this.attributionUrl = props.getProperty("attribution.url", "https://www.openstreetmap.org/copyright");

        this.maxSnapDistanceMeters = parseDouble(props.getProperty("destination.max_snap_distance_meters"), 35.0);
        this.knownLimitations = props.getProperty("known.limitations", "None recorded");
    }

    /**
     * Factory method that loads metadata from the default classpath resource.
     *
     * @return a populated CampusDataMetadata instance
     * @throws IOException if reading the resource fails
     */
    public static CampusDataMetadata loadFromClasspath() throws IOException {
        String resourcePath = "/data/campus-data-metadata.properties";
        try (InputStream in = CampusDataMetadata.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                // Try relative path without leading slash
                try (InputStream in2 = CampusDataMetadata.class.getClassLoader().getResourceAsStream("data/campus-data-metadata.properties")) {
                    if (in2 == null) {
                        throw new IOException("Campus metadata resource not found on classpath: " + resourcePath);
                    }
                    Properties props = new Properties();
                    props.load(in2);
                    return new CampusDataMetadata(props);
                }
            }
            Properties props = new Properties();
            props.load(in);
            return new CampusDataMetadata(props);
        }
    }

    private static double parseDouble(String value, double fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    public String getSourceName() {
        return sourceName;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getExportDate() {
        return exportDate;
    }

    public double getBboxSouth() {
        return bboxSouth;
    }

    public double getBboxWest() {
        return bboxWest;
    }

    public double getBboxNorth() {
        return bboxNorth;
    }

    public double getBboxEast() {
        return bboxEast;
    }

    public String getLicenceName() {
        return licenceName;
    }

    public String getAttributionText() {
        return attributionText;
    }

    public String getAttributionUrl() {
        return attributionUrl;
    }

    public double getMaxSnapDistanceMeters() {
        return maxSnapDistanceMeters;
    }

    public String getKnownLimitations() {
        return knownLimitations;
    }

    @Override
    public String toString() {
        return "CampusDataMetadata{" +
                "source='" + sourceName + '\'' +
                ", date='" + exportDate + '\'' +
                ", bbox=[" + bboxSouth + ", " + bboxWest + ", " + bboxNorth + ", " + bboxEast + "]" +
                '}';
    }
}

