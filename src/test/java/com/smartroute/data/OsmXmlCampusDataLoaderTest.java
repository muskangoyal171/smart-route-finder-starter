package com.smartroute.data;

// Explicit imports with purpose comments
import com.smartroute.model.CampusGraph;               // Graph model holding the loaded graph
import com.smartroute.model.Location;                  // Location destination model
import com.smartroute.model.Location.SnapType;          // Snapping classification enum
import com.smartroute.testutil.SimpleAssert;           // Lightweight assertion utility
import java.io.ByteArrayInputStream;                   // Used for creating in-memory XML fixture streams
import java.io.File;                                   // File interface for disk snapshot loading
import java.io.InputStream;                            // Input stream interface
import java.nio.charset.StandardCharsets;              // Standard UTF-8 charset
import java.nio.file.Files;                            // Files utility
import java.nio.file.Path;                             // File path operations

/**
 * Unit tests verifying {@link OsmXmlCampusDataLoader} parsing behavior, tag allowlist
 * filtering, access exclusions, and destination snapping policy.
 */
public class OsmXmlCampusDataLoaderTest {

    private static final String SAMPLE_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <osm version="0.6">
              <bounds minlat="12.960" minlon="79.150" maxlat="12.980" maxlon="79.170"/>
              <!-- Walkable path nodes -->
              <node id="1" lat="12.9690" lon="79.1550"/>
              <node id="2" lat="12.9695" lon="79.1555"/>
              <node id="3" lat="12.9700" lon="79.1560"/>

              <!-- Nearby destination POI (< 35m from Node 1) -->
              <node id="4" lat="12.96905" lon="79.15505">
                <tag k="name" v="Academic Office"/>
              </node>

              <!-- Remote destination POI (> 100m from all nodes) -->
              <node id="5" lat="12.9780" lon="79.1680">
                <tag k="name" v="Distant Field"/>
              </node>

              <!-- Eligible footway -->
              <way id="101">
                <nd ref="1"/>
                <nd ref="2"/>
                <nd ref="3"/>
                <tag k="highway" v="footway"/>
                <tag k="name" v="Campus Main Walkway"/>
              </way>

              <!-- Rejected highway: arterial primary road -->
              <way id="102">
                <nd ref="1"/>
                <nd ref="2"/>
                <tag k="highway" v="primary"/>
              </way>

              <!-- Rejected highway: foot=no -->
              <way id="103">
                <nd ref="2"/>
                <nd ref="3"/>
                <tag k="highway" v="service"/>
                <tag k="foot" v="no"/>
              </way>
            </osm>
            """;

    public void testFixtureParsingAndFiltering() throws Exception {
        OsmXmlCampusDataLoader loader = new OsmXmlCampusDataLoader(35.0);
        InputStream in = new ByteArrayInputStream(SAMPLE_XML.getBytes(StandardCharsets.UTF_8));

        CampusGraph graph = loader.load(in);

        // Only nodes 1, 2, 3 should be in the walkable graph vertices
        SimpleAssert.assertEquals(3, graph.getVertexCount(), "Walkable vertex count must be 3");
        SimpleAssert.assertTrue(graph.containsNode(1L), "Node 1 must be present");
        SimpleAssert.assertTrue(graph.containsNode(2L), "Node 2 must be present");
        SimpleAssert.assertTrue(graph.containsNode(3L), "Node 3 must be present");

        // Way 101 has 2 segments -> 4 directed edges
        SimpleAssert.assertEquals(4, graph.getTotalDirectedEdgeCount(), "Total directed edges must be 4");

        // Check destination snapping
        Location academicOffice = graph.getLocationByName("Academic Office");
        SimpleAssert.assertNotNull(academicOffice, "Academic Office should be loaded as destination");
        SimpleAssert.assertTrue(academicOffice.isConnected(), "Academic Office should be snapped to adjacent vertex");
        SimpleAssert.assertEquals(1L, academicOffice.getConnectedNodeId(), "Should snap to node 1");
        SimpleAssert.assertEquals(SnapType.PROXIMITY_SNAP, academicOffice.getSnapType(), "Should be PROXIMITY_SNAP");
        SimpleAssert.assertTrue(academicOffice.getSnapDistanceMeters() <= 35.0, "Snap distance should be <= 35m");

        Location distantField = graph.getLocationByName("Distant Field");
        SimpleAssert.assertNotNull(distantField, "Distant Field should be loaded as destination");
        SimpleAssert.assertFalse(distantField.isConnected(), "Distant Field should NOT be snapped (> 35m)");
        SimpleAssert.assertEquals(SnapType.UNCONNECTED, distantField.getSnapType(), "Should be UNCONNECTED");
    }

    public void testRealCampusXmlSnapshotLoading() throws Exception {
        Path xmlPath = Path.of("d:/smart-route-finder-starter/smart-route-finder-starter/src/main/resources/data/vit-vellore-campus.osm.xml");
        SimpleAssert.assertTrue(Files.exists(xmlPath), "vit-vellore-campus.osm.xml must exist");

        OsmXmlCampusDataLoader loader = new OsmXmlCampusDataLoader(35.0);
        CampusGraph graph = loader.load(xmlPath.toFile());

        // Assert graph invariants and metrics verified after adding crossing, unclassified, and track walkways
        SimpleAssert.assertEquals(1643, graph.getVertexCount(), "Walkable graph vertex count should be 1643");
        SimpleAssert.assertEquals(3586, graph.getTotalDirectedEdgeCount(), "1793 non-zero bidirectional segments = 3586 directed edges");
        SimpleAssert.assertEquals(24, graph.countConnectedComponents(), "Should have 24 connected components after pedestrian crossing unification");
        SimpleAssert.assertTrue(graph.getAllLocations().size() > 50, "Should have loaded campus destinations");

        // Validate graph invariants
        SimpleAssert.assertDoesNotThrow(graph::validateInvariants, "Real campus graph must satisfy all invariants");

        // Verify key destinations exist
        Location library = graph.getLocationByName("EV Periyar Library");
        SimpleAssert.assertNotNull(library, "EV Periyar Library should exist in the graph");
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running OsmXmlCampusDataLoaderTest...");
        OsmXmlCampusDataLoaderTest test = new OsmXmlCampusDataLoaderTest();
        test.testFixtureParsingAndFiltering();
        System.out.println("Fixture test PASSED!");

        test.testRealCampusXmlSnapshotLoading();
        System.out.println("Real campus XML snapshot test PASSED!");
        System.out.println("All OsmXmlCampusDataLoaderTest tests PASSED!");
    }
}
