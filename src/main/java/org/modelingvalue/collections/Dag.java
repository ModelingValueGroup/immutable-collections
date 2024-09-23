package org.modelingvalue.collections;

public interface Dag<N> extends DirGraph<N> {
    @Override
    Dag<N> prune(N node);

    @Override
    Dag<N> addNode(N node);

    @Override
    Dag<N> removeEdge(N from, N to);

    @Override
    Dag<N> removeNode(N node);

    @Override
    Dag<N> clear(N node);

    @Override
    Dag<N> clearOuts(N node);

    @Override
    Dag<N> clearIns(N node);

    @Override
    Dag<N> remove(N node, Set<N> ins, Set<N> outs);

    @Override
    Dag<N> removeOuts(N node, Set<N> outs);

    @Override
    Dag<N> removeIns(N node, Set<N> ins);

}
