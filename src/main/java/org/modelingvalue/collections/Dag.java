package org.modelingvalue.collections;

import org.modelingvalue.collections.impl.DirGraphImpl;
import org.modelingvalue.collections.util.TriFunction;

public interface Dag<N> extends DirGraph<N> {

    @SuppressWarnings("unchecked")
    static <E> Dag<E> of() {
        return DirGraphImpl.EMPTY;
    }

    <A> A dfs(A acc, TriFunction<A, N, N, A> func, boolean frwrd);

    @Override
    Dag<N> retainBegin(Set<N> begin);

    @Override
    Dag<N> retainEnd(Set<N> begin);

    @Override
    Dag<N> removeEdge(N from, N to);

    @Override
    Dag<N> clear(N node);

    @Override
    Dag<N> clearOuts(N node);

    @Override
    Dag<N> clearIns(N node);

    @Override
    Dag<N> putBegin(N node, Set<N> outs);

    @Override
    Dag<N> putEnd(N node, Set<N> ins);

    @Override
    Dag<N> remove(N node, Set<N> ins, Set<N> outs);

    @Override
    Dag<N> removeOuts(N node, Set<N> outs);

    @Override
    Dag<N> removeIns(N node, Set<N> ins);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> removeEdges(N... edges);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> putBegin(N node, N... outs);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> putEnd(N node, N... ins);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> removeOuts(N node, N... outs);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> removeIns(N node, N... ins);

    @Override
    Dag<N> remove(Object e);

    @Override
    Dag<N> removeAll(Collection<?> e);

}
