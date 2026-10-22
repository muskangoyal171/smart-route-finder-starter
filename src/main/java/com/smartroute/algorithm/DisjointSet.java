package com.smartroute.algorithm;

// Explicit imports with purpose comments
import java.util.HashMap; // Fast O(1) average lookup for element parents and ranks
import java.util.Map;     // Map interface

/**
 * Disjoint Set Union (DSU) / Union-Find data structure with Path Compression and Union by Rank.
 * <p>
 * <b>DSA Characteristics:</b>
 * <ul>
 *   <li>Path Compression: Flattens the structure of the tree during {@code find()} by making every visited node point directly to the root.</li>
 *   <li>Union by Rank: Always attaches the tree with smaller depth (rank) under the root of the tree with larger depth.</li>
 *   <li>Time Complexity: Amortized {@code O(\alpha(N))} per operation, where {@code \alpha} is the extremely slow-growing Inverse Ackermann function (effectively {@code O(1)} in practice).</li>
 *   <li>Space Complexity: {@code O(N)} to store parent and rank pointers for {@code N} elements.</li>
 * </ul>
 * </p>
 */
public class DisjointSet {

    private final Map<Long, Long> parent;
    private final Map<Long, Integer> rank;
    private int componentCount;

    /**
     * Constructs an empty DisjointSet.
     */
    public DisjointSet() {
        this.parent = new HashMap<>();
        this.rank = new HashMap<>();
        this.componentCount = 0;
    }

    /**
     * Initializes a new disjoint set containing a single element.
     *
     * @param element the identifier to add
     */
    public void makeSet(long element) {
        if (!parent.containsKey(element)) {
            parent.put(element, element);
            rank.put(element, 0);
            componentCount++;
        }
    }

    /**
     * Finds the representative root of the set containing the given element,
     * applying path compression along the way.
     *
     * @param element the element to search for
     * @return the representative root of the set
     */
    public long find(long element) {
        if (!parent.containsKey(element)) {
            makeSet(element);
            return element;
        }

        long root = element;
        while (root != parent.get(root)) {
            root = parent.get(root);
        }

        // Path compression: point all ancestors directly to the root
        long curr = element;
        while (curr != root) {
            long next = parent.get(curr);
            parent.put(curr, root);
            curr = next;
        }

        return root;
    }

    /**
     * Merges the sets containing elements {@code a} and {@code b} using union-by-rank.
     *
     * @param a first element
     * @param b second element
     * @return true if the sets were merged; false if they were already in the same set (cycle detected)
     */
    public boolean union(long a, long b) {
        long rootA = find(a);
        long rootB = find(b);

        if (rootA == rootB) {
            return false; // Already in the same set: adding this edge would create a cycle!
        }

        int rankA = rank.get(rootA);
        int rankB = rank.get(rootB);

        // Union by rank: attach smaller tree under root of larger tree
        if (rankA < rankB) {
            parent.put(rootA, rootB);
        } else if (rankA > rankB) {
            parent.put(rootB, rootA);
        } else {
            parent.put(rootB, rootA);
            rank.put(rootA, rankA + 1);
        }

        componentCount--;
        return true;
    }

    /**
     * Checks if elements {@code a} and {@code b} belong to the same set.
     *
     * @param a first element
     * @param b second element
     * @return true if both belong to the same set, false otherwise
     */
    public boolean connected(long a, long b) {
        return find(a) == find(b);
    }

    public int getComponentCount() {
        return componentCount;
    }

    public int size() {
        return parent.size();
    }
}

