package org.modelingvalue.collections;

import org.modelingvalue.collections.impl.VertexImpl;
import org.modelingvalue.collections.util.Mergeable;

public interface Vertex<N> extends Mergeable<Vertex<N>> {

    static <E> Vertex<E> of(E node, Set<E> ins, Set<E> outs) {
        return new VertexImpl<E>(node, ins, outs);
    }

    N node();

    Set<N> ins();

    Set<N> outs();
}
