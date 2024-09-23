package org.modelingvalue.collections;

import org.modelingvalue.collections.impl.DagImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.SerializableFunction;

public interface DirGraph<N> extends Collection<Vertex<N>>, Mergeable<DirGraph<N>> {

    @SuppressWarnings("rawtypes")
    SerializableFunction<Vertex, Object> NODE_OF_VERTEX = Vertex::node;
    @SuppressWarnings({"unchecked", "rawtypes"})
    Dag                                  EMPTY          = new DagImpl(Set.of(), Set.of(), QualifiedSet.of(NODE_OF_VERTEX));

    @SuppressWarnings("unchecked")
    static <E> Dag<E> empty() {
        return EMPTY;
    }

    Set<N> begin();

    Set<N> end();

    QualifiedSet<N, Vertex<N>> vertices();

    Set<N> ins(N node);

    Set<N> outs(N node);

    DirGraph<N> prune(N node);

    boolean containsNode(N node);

    boolean containsEdge(N from, N to);

    DirGraph<N> addNode(N node);

    DirGraph<N> removeNode(N node);

    DirGraph<N> addEdge(N from, N to);

    DirGraph<N> removeEdge(N from, N to);

    DirGraph<N> clear(N node);

    DirGraph<N> clearOuts(N node);

    DirGraph<N> clearIns(N node);

    DirGraph<N> put(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> putOuts(N node, Set<N> outs);

    DirGraph<N> putIns(N node, Set<N> ins);

    DirGraph<N> add(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> addOuts(N node, Set<N> outs);

    DirGraph<N> addIns(N node, Set<N> ins);

    DirGraph<N> remove(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> removeOuts(N node, Set<N> outs);

    DirGraph<N> removeIns(N node, Set<N> ins);

    Set<Pair<N, N>> cycles();

    List<N> topological();

    Dag<N> removeCycles();

}
