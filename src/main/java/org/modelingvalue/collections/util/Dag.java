package org.modelingvalue.collections.util;

import java.util.Objects;

import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.struct.impl.Struct3Impl;

public final class Dag<N> {

    @SuppressWarnings("rawtypes")
    private static final SerializableFunction<Vertex, Object> NODE_OF_VERTEX = Vertex::node;
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Dag                                  EMPTY          = new Dag(Set.of(), Set.of(), QualifiedSet.of(NODE_OF_VERTEX));

    @SuppressWarnings("unchecked")
    public static <E> Dag<E> empty() {
        return EMPTY;
    }

    private final Set<N>                     start;
    private final Set<N>                     end;
    private final QualifiedSet<N, Vertex<N>> vertices;

    private Dag(Set<N> start, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        this.start = start;
        this.end = end;
        this.vertices = vertices;
    }

    @SuppressWarnings("unchecked")
    public Dag<N> pruneOuts(N node) {
        Vertex<N> vertex = vertices.get(node);
        if (vertex != null) {
            Set<N>[] startEnd = new Set[]{start, end};
            QualifiedSet<N, Vertex<N>> pruned = pruneOuts(vertices, vertex, startEnd);
            return new Dag<N>(startEnd[0], startEnd[1], pruned);
        } else {
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    public Dag<N> pruneIns(N node) {
        Vertex<N> vertex = vertices.get(node);
        if (vertex != null) {
            Set<N>[] startEnd = new Set[]{start, end};
            QualifiedSet<N, Vertex<N>> pruned = pruneIns(vertices, vertex, startEnd);
            return new Dag<N>(startEnd[0], startEnd[1], pruned);
        } else {
            return this;
        }
    }

    private static <E> QualifiedSet<E, Vertex<E>> pruneOuts(QualifiedSet<E, Vertex<E>> vertices, Vertex<E> vertex, Set<E>[] startEnd) {
        QualifiedSet<E, Vertex<E>> pruned = vertices.remove(vertex);
        if (vertex.ins().isEmpty()) {
            startEnd[0] = startEnd[0].remove(vertex.node());
        } else {
            for (E in : vertex.ins()) {
                Vertex<E> vin = vertices.get(in);
                Set<E> outs = vin.outs().remove(vertex.node());
                pruned = pruned.put(Vertex.of(in, vin.ins(), outs));
                if (outs.isEmpty()) {
                    startEnd[1] = startEnd[1].add(in);
                }
            }
        }
        if (vertex.outs().isEmpty()) {
            startEnd[1] = startEnd[1].remove(vertex.node());
        } else {
            for (E out : vertex.outs()) {
                Vertex<E> vout = vertices.get(out);
                if (vout.ins().size() <= 1) {
                    pruned = pruneOuts(pruned, vout, startEnd);
                } else {
                    Set<E> ins = vout.ins().remove(vertex.node());
                    pruned = pruned.put(Vertex.of(out, ins, vout.outs()));
                    if (ins.isEmpty()) {
                        startEnd[0] = startEnd[0].add(out);
                    }
                }
            }
        }
        return pruned;
    }

    private static <E> QualifiedSet<E, Vertex<E>> pruneIns(QualifiedSet<E, Vertex<E>> vertices, Vertex<E> vertex, Set<E>[] startEnd) {
        QualifiedSet<E, Vertex<E>> pruned = vertices.remove(vertex);
        if (vertex.outs().isEmpty()) {
            startEnd[1] = startEnd[1].remove(vertex.node());
        } else {
            for (E out : vertex.outs()) {
                Vertex<E> vout = vertices.get(out);
                Set<E> ins = vout.ins().remove(vertex.node());
                pruned = pruned.put(Vertex.of(out, ins, vout.outs()));
                if (ins.isEmpty()) {
                    startEnd[0] = startEnd[0].add(out);
                }
            }
        }
        for (E in : vertex.ins()) {
            Vertex<E> vin = vertices.get(in);
            if (vin.outs().size() <= 1) {
                pruned = pruneIns(pruned, vin, startEnd);
            } else {
                Set<E> outs = vin.outs().remove(vertex.node());
                pruned = pruned.put(Vertex.of(in, vin.ins(), outs));
                if (outs.isEmpty()) {
                    startEnd[1] = startEnd[1].add(in);
                }
            }
        }
        return pruned;
    }

    public Set<N> ins(N node) {
        Vertex<N> vertex = vertices.get(node);
        return vertex != null ? vertex.ins() : Set.of();
    }

    public Set<N> outs(N node) {
        Vertex<N> vertex = vertices.get(node);
        return vertex != null ? vertex.outs() : Set.of();
    }

    public Set<N> start() {
        return start;
    }

    public Set<N> end() {
        return end;
    }

    public boolean contains(N node) {
        return vertices.containsKey(node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vertices);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        } else {
            Dag<?> other = (Dag<?>) obj;
            return Objects.equals(vertices, other.vertices);
        }
    }

    public final static class Vertex<N> extends Struct3Impl<N, Set<N>, Set<N>> {
        private static final long serialVersionUID = -3578650096220973873L;

        static final <E> Vertex<E> of(E node, Set<E> ins, Set<E> outs) {
            return new Vertex<E>(node, ins, outs);
        }

        private Vertex(N node, Set<N> ins, Set<N> outs) {
            super(node, ins, outs);
        }

        public N node() {
            return get0();
        }

        public Set<N> ins() {
            return get1();
        }

        public Set<N> outs() {
            return get2();
        }
    }

}
