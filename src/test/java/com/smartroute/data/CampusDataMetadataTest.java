package com.smartroute.data;

// Explicit imports with purpose comments
import com.smartroute.testutil.SimpleAssert; // Lightweight assertion utility
import java.io.ByteArrayInputStream;         // Used for creating in-memory property test streams
import java.io.IOException;                  // I/O exception handling
import java.nio.charset.StandardCharsets;    // UTF-8 charset
import java.util.Properties;                 // Properties class

/**
 * Unit tests verifying {@link CampusDataMetadata} parsing, fallback values, and bounds validation.
 */
public class CampusDataMetadataTest {

    public void testMetadataParsing() throws IOException {
        String propContent = """
                source.name=OpenStreetMap
                source.url=https://www.openstreetmap.org
                source.export.date=2026-10-22
                bbox.south=12.9640
                bbox.west=79.1500
                bbox.north=12.9790
                bbox.east=79.1670
                licence.name=Open Database License (ODbL) 1.0
                attribution.text=Map data \\u00a9 OpenStreetMap contributors
                destination.max_snap_distance_meters=35.0
                known.limitations=Test limitations
                """;

        Properties props = new Properties();
        props.load(new ByteArrayInputStream(propContent.getBytes(StandardCharsets.UTF_8)));

        CampusDataMetadata metadata = new CampusDataMetadata(props);

        SimpleAssert.assertEquals("OpenStreetMap", metadata.getSourceName(), "Source name must match");
        SimpleAssert.assertEquals("2026-10-22", metadata.getExportDate(), "Export date must match");
        SimpleAssert.assertEquals(12.9640, metadata.getBboxSouth(), 1e-6, "Bbox south must match");
        SimpleAssert.assertEquals(79.1500, metadata.getBboxWest(), 1e-6, "Bbox west must match");
        SimpleAssert.assertEquals(12.9790, metadata.getBboxNorth(), 1e-6, "Bbox north must match");
        SimpleAssert.assertEquals(79.1670, metadata.getBboxEast(), 1e-6, "Bbox east must match");
        SimpleAssert.assertEquals(35.0, metadata.getMaxSnapDistanceMeters(), 1e-6, "Snap distance must match");
        SimpleAssert.assertTrue(metadata.getAttributionText().contains("OpenStreetMap contributors"), "Attribution text must contain contributor credit");
    }

    public void testMetadataFallbacks() {
        CampusDataMetadata metadata = new CampusDataMetadata(new Properties());

        SimpleAssert.assertEquals("OpenStreetMap", metadata.getSourceName(), "Default source name must be OpenStreetMap");
        SimpleAssert.assertEquals(12.9640, metadata.getBboxSouth(), 1e-6, "Default south must match fallback");
        SimpleAssert.assertEquals(35.0, metadata.getMaxSnapDistanceMeters(), 1e-6, "Default snap threshold must be 35m");
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Running CampusDataMetadataTest...");
        CampusDataMetadataTest test = new CampusDataMetadataTest();
        test.testMetadataParsing();
        test.testMetadataFallbacks();
        System.out.println("All CampusDataMetadataTest tests PASSED!");
    }
}
