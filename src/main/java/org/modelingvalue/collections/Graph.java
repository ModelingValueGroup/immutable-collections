package org.modelingvalue.collections;

import org.modelingvalue.collections.impl.GraphImpl;

public interface Graph<V, E> {
    static <V, E> Graph<V, E> of() {
        return new GraphImpl<>(Map.of());
    }

    Collection<V> getAllNodes();

    Graph<V, E> addNode(V node);

    Graph<V, E> removeNode(V node);

    boolean containsNode(V node);

    Graph<V, E> putEdge(V src, V dest, E val);

    E getEdge(V src, V dest);

    Map<V, E> getIncomingEdges(V node);

    Map<V, E> getOutgoingEdges(V node);

    Graph<V, E> removeEdge(V src, V dest);

    default boolean containsEdge(V src, V dest) {
        return getEdge(src, dest) != null;
    }

    default boolean hasCycles() {
        var safe = new java.util.HashSet<V>();

        nextNode: for (V node : getAllNodes()) {
            var active = new java.util.HashSet<V>();
            var queue = new java.util.LinkedList<V>();
            queue.add(node);

            while (!queue.isEmpty()) {
                V curr = queue.pollFirst();
                active.add(curr);
                if (safe.contains(curr)) continue nextNode;

                for (V next : getOutgoingEdges(curr).toKeys()) {
                    if (active.contains(next)) {
                        return true;
                    } else {
                        queue.add(next);
                    }
                }
            }

            safe.addAll(active);
        }

        return false;
    }

    int size();
}
