package com.smartroute.data;

// Explicit imports with purpose comments
import com.smartroute.model.CampusGraph;               // The graph model constructed by this loader
import com.smartroute.model.GraphNode;                 // Vertex representation in the graph
import com.smartroute.model.Location;                  // Selectable destination model
import com.smartroute.model.Location.SnapType;          // Snapping classification enum
import com.smartroute.model.Road;                      // Edge segment model
import java.io.File;                                   // File representation for local disk loading
import java.io.IOException;                            // I/O exception handling
import java.io.InputStream;                            // Stream interface for reading XML from classpath or file
import java.util.ArrayDeque;                            // Standard FIFO queue for component exploration
import java.util.ArrayList;                            // Dynamic list for way references and tags
import java.util.Collections;                          // Empty collection constants
import java.util.HashMap;                              // Fast node ID coordinate lookup
import java.util.HashSet;                              // Visited nodes and tag checking
import java.util.List;                                 // Ordered node references in ways
import java.util.Map;                                  // Key-value tag mapping
import java.util.Queue;                                // Queue interface for component traversal
import java.util.Set;                                  // Tag allowlist sets
import javax.xml.parsers.DocumentBuilder;              // DOM document builder
import javax.xml.parsers.DocumentBuilderFactory;       // Parser factory with security features
import javax.xml.parsers.ParserConfigurationException; // XML parser configuration errors
import org.w3c.dom.Document;                           // Parsed XML document tree
import org.w3c.dom.Element;                            // XML element node
import org.w3c.dom.NodeList;                           // XML node list iteration
import org.xml.sax.SAXException;                       // XML syntax errors

/**
 * Parses a local OpenStreetMap XML snapshot and constructs a validated {@link CampusGraph}.
 * <p>
 * <b>Security:</b> External DTD and general entity processing are explicitly disabled
 * to prevent XML External Entity (XXE) attacks.
 * </p>
 * <p>
 * <b>Routing Policy:</b> Only ways matching the approved walkable allowlist
 * ({@code footway, path, pedestrian, steps, service, residential, living_street}) are ingested,
 * while explicit pedestrian prohibitions ({@code foot=no}, {@code access=no}) are rejected.
 * Segment distances are calculated in metres using the geodesic Haversine formula.
 * </p>
 */
public class OsmXmlCampusDataLoader {

    private static final Set<String> ALLOWED_HIGHWAYS = Set.of(
            "footway", "path", "pedestrian", "steps", "service", "residential", "living_street",
            "crossing", "unclassified", "track"
    );

    private final double maxSnapDistanceMeters;

    /**
     * Constructs a loader with a specified maximum snapping threshold.
     *
     * @param maxSnapDistanceMeters maximum distance in metres to snap a POI to a path
     */
    public OsmXmlCampusDataLoader(double maxSnapDistanceMeters) {
        this.maxSnapDistanceMeters = maxSnapDistanceMeters > 0.0 ? maxSnapDistanceMeters : 50.0;
    }

    /**
     * Default constructor using a standard 50.0 metre snapping threshold.
     */
    public OsmXmlCampusDataLoader() {
        this(50.0);
    }

    /**
     * Loads and builds a {@link CampusGraph} from an input stream containing raw OSM XML.
     *
     * @param xmlInputStream the input stream of the OSM XML document
     * @return the fully populated and validated CampusGraph
     * @throws IOException                  if reading the stream fails
     * @throws ParserConfigurationException if XML security configuration fails
     * @throws SAXException                 if XML is malformed
     */
    public CampusGraph load(InputStream xmlInputStream) throws IOException, ParserConfigurationException, SAXException {
        if (xmlInputStream == null) {
            throw new IllegalArgumentException("XML input stream cannot be null");
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        // Disable external DTDs and entity expansion for security
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
        dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(xmlInputStream);
        doc.getDocumentElement().normalize();

        return buildGraphFromDocument(doc);
    }

    /**
     * Convenience method to load from a local XML file.
     *
     * @param xmlFile the file on disk
     * @return the populated CampusGraph
     */
    public CampusGraph load(File xmlFile) throws IOException, ParserConfigurationException, SAXException {
        if (xmlFile == null || !xmlFile.exists()) {
            throw new IllegalArgumentException("XML file does not exist: " + xmlFile);
        }
        try (InputStream in = java.nio.file.Files.newInputStream(xmlFile.toPath())) {
            return load(in);
        }
    }

    private CampusGraph buildGraphFromDocument(Document doc) {
        CampusGraph graph = new CampusGraph();

        // 1. Parse all nodes and store coordinates and tags
        NodeList xmlNodes = doc.getElementsByTagName("node");
        Map<Long, GraphNode> allNodes = new HashMap<>();
        List<CandidateDestination> candidateDestinations = new ArrayList<>();

        for (int i = 0; i < xmlNodes.getLength(); i++) {
            Element elem = (Element) xmlNodes.item(i);
            long id = Long.parseLong(elem.getAttribute("id"));
            double lat = Double.parseDouble(elem.getAttribute("lat"));
            double lon = Double.parseDouble(elem.getAttribute("lon"));

            Map<String, String> tags = parseTags(elem);
            GraphNode node = new GraphNode(id, lat, lon, tags);
            allNodes.put(id, node);

            String name = tags.get("name");
            String entrance = tags.get("entrance");
            if (name != null && !name.trim().isEmpty()) {
                candidateDestinations.add(new CandidateDestination("node_" + id, name.trim(), lat, lon, id, entrance != null));
            } else if (entrance != null) {
                candidateDestinations.add(new CandidateDestination("entrance_" + id, "Entrance (" + id + ")", lat, lon, id, true));
            }
        }

        // 2. Parse ways and construct walkable edges
        NodeList xmlWays = doc.getElementsByTagName("way");
        Set<Long> routableNodeIds = new HashSet<>();

        for (int i = 0; i < xmlWays.getLength(); i++) {
            Element elem = (Element) xmlWays.item(i);
            long wayId = Long.parseLong(elem.getAttribute("id"));
            Map<String, String> tags = parseTags(elem);

            String wayName = tags.get("name");
            String highway = tags.get("highway");

            // Track named ways as candidate destinations (e.g. library, sports courts, roads)
            if (wayName != null && !wayName.trim().isEmpty()) {
                double[] centroid = calculateWayCentroid(elem, allNodes);
                if (centroid != null) {
                    candidateDestinations.add(new CandidateDestination("way_" + wayId, wayName.trim(), centroid[0], centroid[1], -1L, false));
                }
            }

            if (highway == null) continue;

            String foot = tags.get("foot");
            String access = tags.get("access");

            // Evaluate against allowlist and exclusions
            boolean isAllowed = ALLOWED_HIGHWAYS.contains(highway)
                    && !"no".equalsIgnoreCase(foot)
                    && !"private".equalsIgnoreCase(foot)
                    && !"no".equalsIgnoreCase(access);

            if (isAllowed) {
                NodeList ndList = elem.getElementsByTagName("nd");
                List<Long> refs = new ArrayList<>();
                for (int j = 0; j < ndList.getLength(); j++) {
                    Element nd = (Element) ndList.item(j);
                    long ref = Long.parseLong(nd.getAttribute("ref"));
                    refs.add(ref);
                }

                // Create edges between consecutive nodes
                for (int j = 0; j < refs.size() - 1; j++) {
                    long uId = refs.get(j);
                    long vId = refs.get(j + 1);

                    GraphNode u = allNodes.get(uId);
                    GraphNode v = allNodes.get(vId);

                    if (u != null && v != null) {
                        double distance = CampusGraph.calculateHaversineDistance(
                                u.getLatitude(), u.getLongitude(),
                                v.getLatitude(), v.getLongitude()
                        );

                        // Skip zero-length identical consecutive nodes
                        if (distance >= 0.1) {
                            graph.addNode(u);
                            graph.addNode(v);
                            routableNodeIds.add(uId);
                            routableNodeIds.add(vId);

                            Road road = new Road(uId, vId, distance, wayId, highway, wayName);
                            graph.addBidirectionalRoad(road);
                        }
                    }
                }
            }
        }

        // 3. Identify the largest connected component to ensure maximum route reachability
        Set<Long> mainComponent = findLargestComponent(graph, routableNodeIds);

        // 4. Disambiguate duplicate destination names (e.g. "Nescafe (1)", "Nescafe (2)")
        Map<String, Integer> nameCounts = new HashMap<>();
        for (CandidateDestination cand : candidateDestinations) {
            String lower = cand.name.toLowerCase();
            nameCounts.put(lower, nameCounts.getOrDefault(lower, 0) + 1);
        }

        Map<String, Integer> currentOccurrence = new HashMap<>();
        for (CandidateDestination cand : candidateDestinations) {
            String displayName = cand.name;
            String lower = cand.name.toLowerCase();
            if (nameCounts.get(lower) > 1) {
                int index = currentOccurrence.getOrDefault(lower, 0) + 1;
                currentOccurrence.put(lower, index);
                displayName = cand.name + " (" + index + ")";
            }

            CandidateDestination disambiguatedCand = new CandidateDestination(
                    cand.id, displayName, cand.latitude, cand.longitude, cand.originalNodeId, cand.isEntrance
            );
            Location location = snapDestination(disambiguatedCand, mainComponent, routableNodeIds, allNodes);
            graph.addLocation(location);
        }

        graph.validateInvariants();
        return graph;
    }

    private Set<Long> findLargestComponent(CampusGraph graph, Set<Long> routableNodeIds) {
        Set<Long> visited = new HashSet<>();
        Set<Long> largest = new HashSet<>();

        for (Long nodeId : routableNodeIds) {
            if (!visited.contains(nodeId)) {
                Set<Long> comp = new HashSet<>();
                Queue<Long> queue = new ArrayDeque<>();
                queue.add(nodeId);
                visited.add(nodeId);
                comp.add(nodeId);

                while (!queue.isEmpty()) {
                    Long u = queue.poll();
                    for (Road edge : graph.getNeighbors(u)) {
                        long v = edge.getEndNodeId();
                        if (!visited.contains(v)) {
                            visited.add(v);
                            comp.add(v);
                            queue.add(v);
                        }
                    }
                }

                if (comp.size() > largest.size()) {
                    largest = comp;
                }
            }
        }
        return largest;
    }

    private Location snapDestination(CandidateDestination cand, Set<Long> mainComponentNodeIds, Set<Long> routableNodeIds, Map<Long, GraphNode> allNodes) {
        // Priority 1: If it's an explicit node directly on the walkable network
        if (cand.originalNodeId != -1L && routableNodeIds.contains(cand.originalNodeId)) {
            SnapType type = cand.isEntrance ? SnapType.MAPPED_ENTRANCE : SnapType.DIRECT_VERTEX;
            return new Location(cand.id, cand.name, cand.latitude, cand.longitude, cand.originalNodeId, type, 0.0);
        }

        // Priority 2: Prioritize snapping to the MAIN campus component to ensure reliable point-to-point routing
        long closestMainId = -1L;
        double minMainDist = Double.MAX_VALUE;

        for (Long nodeId : mainComponentNodeIds) {
            GraphNode node = allNodes.get(nodeId);
            if (node != null) {
                double dist = CampusGraph.calculateHaversineDistance(
                        cand.latitude, cand.longitude,
                        node.getLatitude(), node.getLongitude()
                );
                if (dist < minMainDist) {
                    minMainDist = dist;
                    closestMainId = nodeId;
                }
            }
        }

        if (closestMainId != -1L && minMainDist <= maxSnapDistanceMeters) {
            return new Location(cand.id, cand.name, cand.latitude, cand.longitude, closestMainId, SnapType.PROXIMITY_SNAP, minMainDist);
        }

        // Priority 3: Fall back to other routable components within maxSnapDistanceMeters
        long closestAnyId = -1L;
        double minAnyDist = Double.MAX_VALUE;

        for (Long nodeId : routableNodeIds) {
            GraphNode node = allNodes.get(nodeId);
            if (node != null) {
                double dist = CampusGraph.calculateHaversineDistance(
                        cand.latitude, cand.longitude,
                        node.getLatitude(), node.getLongitude()
                );
                if (dist < minAnyDist) {
                    minAnyDist = dist;
                    closestAnyId = nodeId;
                }
            }
        }

        if (closestAnyId != -1L && minAnyDist <= maxSnapDistanceMeters) {
            return new Location(cand.id, cand.name, cand.latitude, cand.longitude, closestAnyId, SnapType.PROXIMITY_SNAP, minAnyDist);
        }

        // Priority 4: Truly detached destination (> maxSnapDistanceMeters)
        return new Location(cand.id, cand.name, cand.latitude, cand.longitude, -1L, SnapType.UNCONNECTED, minAnyDist);
    }

    private double[] calculateWayCentroid(Element wayElem, Map<Long, GraphNode> allNodes) {
        NodeList ndList = wayElem.getElementsByTagName("nd");
        if (ndList.getLength() == 0) return null;

        double sumLat = 0;
        double sumLon = 0;
        int count = 0;

        for (int i = 0; i < ndList.getLength(); i++) {
            Element nd = (Element) ndList.item(i);
            long ref = Long.parseLong(nd.getAttribute("ref"));
            GraphNode n = allNodes.get(ref);
            if (n != null) {
                sumLat += n.getLatitude();
                sumLon += n.getLongitude();
                count++;
            }
        }

        if (count == 0) return null;
        return new double[]{sumLat / count, sumLon / count};
    }

    private Map<String, String> parseTags(Element parentElem) {
        NodeList tagNodes = parentElem.getElementsByTagName("tag");
        if (tagNodes.getLength() == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> tags = new HashMap<>();
        for (int i = 0; i < tagNodes.getLength(); i++) {
            Element tagElem = (Element) tagNodes.item(i);
            String k = tagElem.getAttribute("k");
            String v = tagElem.getAttribute("v");
            if (k != null && v != null) {
                tags.put(k, v);
            }
        }
        return tags;
    }

    private static class CandidateDestination {
        final String id;
        final String name;
        final double latitude;
        final double longitude;
        final long originalNodeId;
        final boolean isEntrance;

        CandidateDestination(String id, String name, double latitude, double longitude, long originalNodeId, boolean isEntrance) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.originalNodeId = originalNodeId;
            this.isEntrance = isEntrance;
        }
    }
}

