package org.modelingvalue.collections.impl;

import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.Vertex;
import org.modelingvalue.collections.struct.impl.Struct3Impl;

public final class VertexImpl<N> extends Struct3Impl<N, Set<N>, Set<N>> implements Vertex<N> {
    private static final long serialVersionUID = -3578650096220973873L;

    public VertexImpl(N node, Set<N> ins, Set<N> outs) {
        super(node, ins, outs);
    }

    @Override
    public N node() {
        return get0();
    }

    @Override
    public Set<N> ins() {
        return get1();
    }

    @Override
    public Set<N> outs() {
        return get2();
    }

    @Override
    public Vertex<N> merge(Vertex<N>[] branches, int length) {
        Set<N> ins = ins();
        Set<N> outs = outs();
        for (int i = 0; i < length; i++) {
            ins = ins.addAll(branches[i].ins());
            outs = outs.addAll(branches[i].outs());
        }
        return new VertexImpl<N>(node(), ins, outs);
    }

    @Override
    public Vertex<N> getMerger() {
        return new VertexImpl<N>(node(), Set.of(), Set.of());
    }

    @Override
    public Class<?> getMeetClass() {
        return Vertex.class;
    }
}
