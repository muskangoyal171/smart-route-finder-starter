package com.smartroute.algorithm;

// Explicit imports with purpose comments
import com.smartroute.model.Road; // Road edge model
import java.util.Collections;     // Used for unmodifiable list wrappers
import java.util.List;            // List interface for tree edges

/**
 * Encapsulates the result of a Minimum Spanning Tree (MST) or Minimum Spanning Forest (MSF) calculation.
 * <p>
 * Contains the list of selected road segments, the total network weight in metres,
 * the number of vertices spanned, and metadata distinguishing tree vs. forest representations.
 * </p>
 */
public class MstResult {

    private final String algorithmName;
    private final List<Road> edges;
    private final double totalWeightMeters;
    private final int spannedVertexCount;
    private final boolean isForest;

    /**
     * Constructs an MstResult instance.
     *
     * @param algorithmName      name of the algorithm used ("Prim" or "Kruskal")
     * @param edges              selected edges in the spanning tree/forest
     * @param totalWeightMeters  sum of all edge weights in metres
     * @param spannedVertexCount number of unique vertices connected in the tree/forest
     * @param isForest           true if the graph contained multiple disconnected components
     */
    public MstResult(String algorithmName, List<Road> edges, double totalWeightMeters,
                     int spannedVertexCount, boolean isForest) {
        this.algorithmName = algorithmName != null ? algorithmName : "MST";
        this.edges = edges != null ? List.copyOf(edges) : Collections.emptyList();
        this.totalWeightMeters = Math.max(0.0, totalWeightMeters);
        this.spannedVertexCount = Math.max(0, spannedVertexCount);
        this.isForest = isForest;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public List<Road> getEdges() {
        return edges;
    }

    public double getTotalWeightMeters() {
        return totalWeightMeters;
    }

    public int getSpannedVertexCount() {
        return spannedVertexCount;
    }

    public int getEdgeCount() {
        return edges.size();
    }

    public boolean isForest() {
        return isForest;
    }

    @Override
    public String toString() {
        return algorithmName + " " + (isForest ? "Forest" : "Tree") + "{" +
                "edges=" + edges.size() +
                ", vertices=" + spannedVertexCount +
                ", totalWeight=" + String.format("%.1f", totalWeightMeters) + "m" +
                '}';
    }
}

