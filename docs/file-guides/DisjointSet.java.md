# `DisjointSet.java` — File Guide

## Purpose

`DisjointSet` (also known as Union-Find) is the foundational data structure used by Kruskal's algorithm to detect and prevent cycles during edge selection. It maintains a collection of disjoint dynamic sets over 64-bit integer vertex IDs, featuring both **Path Compression** and **Union by Rank**.

## Where It Is Used

- **Package:** `com.smartroute.algorithm`
- **Kruskal's Algorithm:** Used by `KruskalMinimumSpanningTree` to check if adding an edge $u \leftrightarrow v$ creates a cycle.
- **Unit Testing:** Thoroughly validated in `MstTest.testDisjointSetOperations()`.

## Explicit Imports and Purpose

- `java.util.HashMap`, `Map`: Efficient $O(1)$ average parent pointer and rank table storage.

## Key Methods and Implementation Details

1. **`makeSet(long element)`:** Initializes a new set with `parent[x] = x` and `rank[x] = 0`.
2. **`find(long element)` (with Path Compression):** Finds the representative root of the set containing `element`. During traversal, flattens the tree by redirecting every visited ancestor to point directly to the root:
   ```java
   long root = element;
   while (root != parent.get(root)) root = parent.get(root);
   // Path compression
   long curr = element;
   while (curr != root) {
       long next = parent.get(curr);
       parent.put(curr, root);
       curr = next;
   }
   return root;
   ```
3. **`union(long a, long b)` (with Union by Rank):** Merges the trees containing `a` and `b`. Attaches the lower-rank root under the higher-rank root. If ranks are equal, increments the root rank by 1. Returns `false` if `find(a) == find(b)` (cycle detected!).

## Complexity Analysis

- **Time Complexity:** Amortized $O(\alpha(V))$ per operation, where $\alpha$ is the Inverse Ackermann function. For all practical values of $V < 10^{80}$, $\alpha(V) \le 4$, making operations effectively $O(1)$.
- **Space Complexity:** $O(V)$ auxiliary storage for parent and rank maps.

## Viva Discussion Points

- *What is the role of Union-Find in Kruskal's algorithm?*  
  It prevents cycles. Before adding an edge $(u, v)$ to the MST, we check `find(u) == find(v)`. If they share the same root, adding the edge would form a closed loop and violate the tree property.

