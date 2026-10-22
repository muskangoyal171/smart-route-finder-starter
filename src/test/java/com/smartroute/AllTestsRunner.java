package com.smartroute;

// Explicit imports with purpose comments
import com.smartroute.algorithm.DijkstraShortestPathTest; // Shortest path routing test suite
import com.smartroute.algorithm.MstTest;                 // Minimum spanning tree and traversal test suite
import com.smartroute.data.CampusDataMetadataTest;       // Metadata test suite
import com.smartroute.data.OsmXmlCampusDataLoaderTest;   // XML loader and snapping test suite
import com.smartroute.model.CampusGraphTest;             // Graph data structure test suite
import com.smartroute.ui.CampusMapRendererTest;          // Vector map projection and rendering test suite
import com.smartroute.ui.UserExperienceFlowTest;         // UX interaction and error handling test suite

/**
 * Main test runner executing all unit tests in the test suite and reporting actual outcomes.
 */
public class AllTestsRunner {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("       SMART ROUTE FINDER — AUTOMATED TESTS       ");
        System.out.println("=================================================");

        int passed = 0;
        int failed = 0;

        // 1. CampusGraphTest
        try {
            System.out.println("\n[1/7] Running CampusGraphTest...");
            CampusGraphTest.main(new String[0]);
            passed++;
        } catch (Throwable t) {
            System.err.println("FAILED: CampusGraphTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 2. CampusDataMetadataTest
        try {
            System.out.println("\n[2/7] Running CampusDataMetadataTest...");
            CampusDataMetadataTest.main(new String[0]);
            passed++;
        } catch (Throwable t) {
            System.err.println("FAILED: CampusDataMetadataTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 3. OsmXmlCampusDataLoaderTest
        try {
            System.out.println("\n[3/7] Running OsmXmlCampusDataLoaderTest...");
            OsmXmlCampusDataLoaderTest.main(new String[0]);
            passed++;
        } catch (Throwable t) {
            System.err.println("FAILED: OsmXmlCampusDataLoaderTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 4. DijkstraShortestPathTest
        try {
            System.out.println("\n[4/7] Running DijkstraShortestPathTest...");
            DijkstraShortestPathTest.main(new String[0]);
            passed++;
        } catch (Throwable t) {
            System.err.println("FAILED: DijkstraShortestPathTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 5. MstTest
        try {
            System.out.println("\n[5/7] Running MstTest...");
            MstTest.main(new String[0]);
            passed++;
        } catch (Throwable t) {
            System.err.println("FAILED: MstTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 6. CampusMapRendererTest
        try {
            System.out.println("\n[6/7] Running CampusMapRendererTest...");
            CampusMapRendererTest.runTests();
            passed++;
        } catch (Throwable t) {
            System.err.println("FAILED: CampusMapRendererTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        // 7. UserExperienceFlowTest
        try {
            System.out.println("\n[7/7] Running UserExperienceFlowTest...");
            UserExperienceFlowTest.main(new String[0]);
            passed++;
        } catch (Throwable t) {
            System.err.println("FAILED: UserExperienceFlowTest: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        System.out.println("\n=================================================");
        System.out.println("TEST RESULTS: " + passed + " passed, " + failed + " failed.");
        System.out.println("=================================================");

        System.exit(failed > 0 ? 1 : 0);
    }
}
