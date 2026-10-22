package com.smartroute.data;

// Explicit imports with purpose comments
import com.smartroute.model.CampusGraph;               // Graph model holding vertices, edges, and destinations
import com.smartroute.model.Location;                  // Selectable destination model
import java.io.IOException;                            // I/O error handling during resource reading
import java.io.InputStream;                            // Input stream for reading resources
import java.util.Collections;                          // Unmodifiable collection wrappers
import java.util.List;                                 // Destination list interface
import javax.xml.parsers.ParserConfigurationException; // Parser configuration exception
import org.xml.sax.SAXException;                       // XML syntax error handling

/**
 * Repository providing access to the loaded VIT Vellore campus graph, destinations, and metadata.
 * <p>
 * This class abstracts the data loading process away from the user interface and routing
 * algorithms. It loads data from local classpath resources without making external network calls.
 * </p>
 */
public class CampusDataRepository {

    private static CampusDataRepository instance;

    private final CampusGraph graph;
    private final CampusDataMetadata metadata;

    /**
     * Constructs a repository with pre-loaded graph and metadata.
     *
     * @param graph    the loaded and validated CampusGraph
     * @param metadata the loaded provenance metadata
     */
    public CampusDataRepository(CampusGraph graph, CampusDataMetadata metadata) {
        if (graph == null) {
            throw new IllegalArgumentException("CampusGraph cannot be null");
        }
        this.graph = graph;
        this.metadata = metadata != null ? metadata : new CampusDataMetadata(null);
    }

    /**
     * Loads the default campus dataset from the classpath ({@code vit-vellore-campus.osm.xml}
     * and {@code campus-data-metadata.properties}).
     *
     * @return a fully populated CampusDataRepository
     * @throws IOException                  if loading resource files fails
     * @throws ParserConfigurationException if XML security configuration fails
     * @throws SAXException                 if XML parsing fails
     */
    public static synchronized CampusDataRepository loadDefault()
            throws IOException, ParserConfigurationException, SAXException {
        if (instance == null) {
            CampusDataMetadata metadata = CampusDataMetadata.loadFromClasspath();

            String xmlPath = "/data/vit-vellore-campus.osm.xml";
            InputStream xmlStream = CampusDataRepository.class.getResourceAsStream(xmlPath);
            if (xmlStream == null) {
                xmlStream = CampusDataRepository.class.getClassLoader().getResourceAsStream("data/vit-vellore-campus.osm.xml");
            }
            if (xmlStream == null) {
                throw new IOException("Campus XML dataset not found on classpath: " + xmlPath);
            }

            try (InputStream streamToUse = xmlStream) {
                OsmXmlCampusDataLoader loader = new OsmXmlCampusDataLoader(metadata.getMaxSnapDistanceMeters());
                CampusGraph graph = loader.load(streamToUse);
                instance = new CampusDataRepository(graph, metadata);
            }
        }
        return instance;
    }

    /**
     * Resets the singleton instance (primarily used in tests).
     */
    public static synchronized void reset() {
        instance = null;
    }

    public CampusGraph getGraph() {
        return graph;
    }

    public CampusDataMetadata getMetadata() {
        return metadata;
    }

    public List<Location> getDestinations() {
        return graph.getAllLocations();
    }

    public Location findLocation(String name) {
        return graph.getLocationByName(name);
    }
}
