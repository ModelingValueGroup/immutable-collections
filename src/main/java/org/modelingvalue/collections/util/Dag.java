package org.modelingvalue.collections.util;

import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;

public class Dag<N> extends DirGraph<N> {

    private static final long serialVersionUID = 1419504275082662008L;

    protected Dag(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        super(begin, end, vertices);
    }

    @Override
    protected Dag<N> construct(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        return new Dag<N>(begin, end, vertices);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> pruneOuts(N node) {
        return (Dag<N>) super.pruneOuts(node);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> pruneIns(N node) {
        return (Dag<N>) super.pruneIns(node);
    }

}
