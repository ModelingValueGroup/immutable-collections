package org.modelingvalue.collections.impl;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Dag;
import org.modelingvalue.collections.DirGraph;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.Vertex;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.TriFunction;

public class DirGraphImpl<N> extends CollectionImpl<Vertex<N>> implements DirGraph<N> {
    private static final long                serialVersionUID = -1977417266823883798L;

    private final Set<N>                     begin;
    private final Set<N>                     end;
    private final QualifiedSet<N, Vertex<N>> vertices;

    public DirGraphImpl(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        this.begin = begin;
        this.end = end;
        this.vertices = vertices;
    }

    protected DirGraph<N> construct(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        return new DirGraphImpl<N>(begin, end, vertices);
    }

    @SuppressWarnings("unchecked")
    protected <A> A dfs(A acc, TriFunction<A, N, N, A> func) {
        Set<N>[] tempPerm = new Set[]{Set.of(), Set.of()};
        for (N node : begin()) {
            acc = visit(acc, vertices.get(node), null, func, tempPerm);
        }
        return acc;
    }

    private <A> A visit(A a, Vertex<N> v, Vertex<N> c, TriFunction<A, N, N, A> f, Set<N>[] tp) {
        if (tp[1].contains(v.node())) {
            return a;
        } else if (tp[0].contains(v.node())) {
            // cycle
            assert c != null;
            return f.apply(a, v.node(), c.node());
        } else {
            tp[0] = tp[0].add(v.node());
            for (N n : v.outs()) {
                a = visit(a, vertices.get(n), v, f, tp);
            }
            tp[1] = tp[1].add(v.node());
            return f.apply(a, v.node(), null);
        }
    }

    @Override
    public Set<Pair<N, N>> cycles() {
        return dfs(Set.of(), (s, n, c) -> c != null ? s.add(Pair.of(c, n)) : s);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> removeCycles() {
        Set<Pair<N, N>> cycles = cycles();
        Set<N> b = begin, e = end;
        QualifiedSet<N, Vertex<N>> v = vertices;
        for (Pair<N, N> c : cycles) {
            Vertex<N> f = v.get(c.a());
            Vertex<N> t = v.get(c.b());
            assert f.outs().contains(t.node()) && t.ins().contains(f.node());
            Set<N> fo = f.outs().remove(t.node());
            if (fo.isEmpty()) {
                e = e.add(f.node());
            }
            Set<N> ti = t.ins().remove(f.node());
            if (ti.isEmpty()) {
                b = b.add(t.node());
            }
            v = v.put(Vertex.of(f.node(), f.ins(), fo));
            v = v.put(Vertex.of(t.node(), ti, t.outs()));
        }
        return new DagImpl<N>(b, e, v);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<N> topological() {
        return dfs(List.of(), (l, n, c) -> c != null ? l : l.add(n));
    }

    @Override
    @SuppressWarnings("unchecked")
    public DirGraph<N> prune(N node) {
        Vertex<N> vertex = vertices.get(node);
        if (vertex != null) {
            Set<N>[] beginEnd = new Set[]{begin, end};
            QualifiedSet<N, Vertex<N>> pruned = prune(vertices, vertex, beginEnd);
            return construct(beginEnd[0], beginEnd[1], pruned);
        } else {
            return this;
        }
    }

    private static <E> QualifiedSet<E, Vertex<E>> prune(QualifiedSet<E, Vertex<E>> vertices, Vertex<E> vertex, Set<E>[] beginEnd) {
        QualifiedSet<E, Vertex<E>> pruned = vertices.remove(vertex);
        if (vertex.ins().isEmpty()) {
            beginEnd[0] = beginEnd[0].remove(vertex.node());
        } else {
            for (E in : vertex.ins()) {
                Vertex<E> vin = vertices.get(in);
                Set<E> outs = vin.outs().remove(vertex.node());
                pruned = pruned.put(Vertex.of(in, vin.ins(), outs));
                if (outs.isEmpty()) {
                    beginEnd[1] = beginEnd[1].add(in);
                }
            }
        }
        if (vertex.outs().isEmpty()) {
            beginEnd[1] = beginEnd[1].remove(vertex.node());
        } else {
            for (E out : vertex.outs()) {
                Vertex<E> vout = vertices.get(out);
                if (vout.ins().size() <= 1) {
                    pruned = prune(pruned, vout, beginEnd);
                } else {
                    Set<E> ins = vout.ins().remove(vertex.node());
                    pruned = pruned.put(Vertex.of(out, ins, vout.outs()));
                }
            }
        }
        return pruned;
    }

    @Override
    public Set<N> ins(N node) {
        Vertex<N> vertex = vertices.get(node);
        return vertex != null ? vertex.ins() : Set.of();
    }

    @Override
    public Set<N> outs(N node) {
        Vertex<N> vertex = vertices.get(node);
        return vertex != null ? vertex.outs() : Set.of();
    }

    @Override
    public Set<N> begin() {
        return begin;
    }

    @Override
    public Set<N> end() {
        return end;
    }

    @Override
    public QualifiedSet<N, Vertex<N>> vertices() {
        return vertices;
    }

    @Override
    public boolean containsNode(N node) {
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
            DirGraphImpl<?> other = (DirGraphImpl<?>) obj;
            return Objects.equals(vertices, other.vertices);
        }
    }

    @Override
    public Iterator<Vertex<N>> iterator() {
        return vertices.iterator();
    }

    @Override
    public void forEach(Consumer<? super Vertex<N>> action) {
        vertices.forEach(action);
    }

    @Override
    public Spliterator<Vertex<N>> spliterator() {
        return vertices.spliterator();
    }

    @Override
    public int size() {
        return vertices.size();
    }

    @Override
    public boolean isEmpty() {
        return vertices.isEmpty();
    }

    @Override
    public <R> Collection<R> linked(TriFunction<Vertex<N>, Vertex<N>, Vertex<N>, R> function) {
        return vertices.linked(function);
    }

    @Override
    public void linked(TriConsumer<Vertex<N>, Vertex<N>, Vertex<N>> consumer) {
        vertices.linked(consumer);
    }

    @Override
    public <R> Collection<R> indexed(BiFunction<Vertex<N>, Integer, R> function) {
        return vertices.indexed(function);
    }

    @Override
    protected Stream<Vertex<N>> baseStream() {
        return new StreamCollectionImpl<>(spliterator(), isParallel());
    }

    @Override
    public boolean contains(Object object) {
        return vertices.contains(object);
    }

    @Override
    public Class<?> getMeetClass() {
        return DirGraph.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> merge(DirGraph<N>[] branches, int length) {
        Set<N>[] ba = new Set[length], ea = new Set[length];
        QualifiedSet<N, Vertex<N>>[] va = new QualifiedSet[length];
        for (int i = 0; i < length; i++) {
            ba[i] = branches[i].begin();
            ea[i] = branches[i].end();
            va[i] = branches[i].vertices();
        }
        Set<N> mb = begin.merge(null);
        Set<N> me = end.merge(null);
        QualifiedSet<N, Vertex<N>> mv = vertices.merge(va);
        return new DirGraphImpl<N>(mb.filter(b -> mv.get(b).ins().isEmpty()).asSet(), me.filter(e -> mv.get(e).outs().isEmpty()).asSet(), mv);
    }

    @Override
    public DirGraph<N> getMerger() {
        return DirGraph.empty();
    }

    @Override
    public boolean containsEdge(N from, N to) {
        return outs(from).contains(to);
    }

    @Override
    public DirGraph<N> addNode(N node) {
        return containsNode(node) ? this : construct(begin.add(node), end.add(node), vertices);
    }

    @Override
    public DirGraph<N> removeNode(N node) {
        if (containsNode(node)) {
            return construct(begin.remove(node), end.remove(node), clear(node).vertices().removeKey(node));
        } else {
            return this;
        }
    }

    @Override
    public DirGraph<N> addEdge(N from, N to) {
        Set<N> fouts = outs(from);
        if (fouts.contains(to)) {
            return this;
        } else {
            Set<N> b = begin, e = end;
            QualifiedSet<N, Vertex<N>> v = vertices;
            if (fouts.isEmpty()) {
                e = e.remove(from);
            }
            Set<N> tins = ins(to);
            if (tins.isEmpty()) {
                b = b.remove(to);
            }
            v = v.put(Vertex.of(from, ins(from), fouts.add(to)));
            v = v.put(Vertex.of(to, tins.add(from), outs(to)));
            return new DirGraphImpl<N>(b, e, v);
        }

    }

    @Override
    public DirGraph<N> removeEdge(N from, N to) {
        Set<N> fouts = outs(from);
        if (fouts.contains(to)) {
            Set<N> b = begin, e = end;
            QualifiedSet<N, Vertex<N>> v = vertices;
            Set<N> fo = fouts.remove(to);
            if (fo.isEmpty()) {
                e = e.add(from);
            }
            Set<N> ti = ins(to).remove(from);
            if (ti.isEmpty()) {
                b = b.add(to);
            }
            v = v.put(Vertex.of(from, ins(from), fo));
            v = v.put(Vertex.of(to, ti, outs(to)));
            return construct(b, e, v);
        } else {
            return this;
        }
    }

    @Override
    public DirGraph<N> put(N node, Set<N> ins, Set<N> outs) {
        Set<N> is = ins(node);
        Set<N> os = outs(node);
        return remove(node, is.removeAll(ins), os.removeAll(outs)).add(node, ins.removeAll(is), outs.removeAll(os));
    }

    @Override
    public DirGraph<N> putOuts(N node, Set<N> outs) {
        Set<N> os = outs(node);
        return removeOuts(node, os.removeAll(outs)).addOuts(node, outs.removeAll(os));
    }

    @Override
    public DirGraph<N> putIns(N node, Set<N> ins) {
        Set<N> is = ins(node);
        return removeIns(node, is.removeAll(ins)).addIns(node, ins.removeAll(is));
    }

    @Override
    public DirGraph<N> add(N node, Set<N> ins, Set<N> outs) {
        return addIns(node, ins).addOuts(node, outs);
    }

    @Override
    public DirGraph<N> clear(N node) {
        return remove(node, ins(node), outs(node));
    }

    @Override
    public DirGraph<N> clearOuts(N node) {
        return removeOuts(node, outs(node));
    }

    @Override
    public DirGraph<N> clearIns(N node) {
        return removeIns(node, ins(node));
    }

    @Override
    public DirGraph<N> remove(N node, Set<N> ins, Set<N> outs) {
        return removeIns(node, ins).removeOuts(node, outs);
    }

    @Override
    public DirGraph<N> addOuts(N node, Set<N> outs) {
        if (outs.isEmpty()) {
            return this;
        } else {
            Set<N> nouts = outs(node);
            if (nouts.containsAll(outs)) {
                return this;
            } else {
                Set<N> b = begin, e = end;
                QualifiedSet<N, Vertex<N>> v = vertices;
                if (nouts.isEmpty()) {
                    e = e.remove(node);
                }
                v = v.put(Vertex.of(node, ins(node), nouts.addAll(outs)));
                for (N out : outs) {
                    Set<N> oins = ins(out);
                    if (oins.isEmpty()) {
                        b = b.remove(out);
                    }
                    v = v.put(Vertex.of(out, oins.add(node), outs(out)));
                }
                return new DirGraphImpl<N>(b, e, v);
            }
        }
    }

    @Override
    public DirGraph<N> addIns(N node, Set<N> ins) {
        if (ins.isEmpty()) {
            return this;
        } else {
            Set<N> nins = ins(node);
            if (nins.containsAll(ins)) {
                return this;
            } else {
                Set<N> b = begin, e = end;
                QualifiedSet<N, Vertex<N>> v = vertices;
                if (nins.isEmpty()) {
                    b = b.remove(node);
                }
                v = v.put(Vertex.of(node, nins.addAll(ins), outs(node)));
                for (N in : ins) {
                    Set<N> iouts = outs(in);
                    if (iouts.isEmpty()) {
                        e = e.remove(in);
                    }
                    v = v.put(Vertex.of(in, ins(in), iouts.add(node)));
                }
                return new DirGraphImpl<N>(b, e, v);
            }
        }
    }

    @Override
    public DirGraph<N> removeOuts(N node, Set<N> outs) {
        if (outs.isEmpty()) {
            return this;
        } else {
            Set<N> nouts = outs(node);
            if (nouts.noneMatch(outs::contains)) {
                return this;
            } else {
                Set<N> b = begin, e = end;
                QualifiedSet<N, Vertex<N>> v = vertices;
                nouts = nouts.removeAll(outs);
                v = v.put(Vertex.of(node, ins(node), nouts));
                if (nouts.isEmpty()) {
                    e = e.add(node);
                }
                for (N out : outs) {
                    Vertex<N> ov = v.get(out);
                    Set<N> ins = ov.ins().remove(node);
                    v.put(Vertex.of(out, ins, ov.outs()));
                    if (ins.isEmpty()) {
                        b = b.add(out);
                    }
                }
                return construct(b, e, v);
            }
        }
    }

    @Override
    public DirGraph<N> removeIns(N node, Set<N> ins) {
        if (ins.isEmpty()) {
            return this;
        } else {
            Set<N> nins = ins(node);
            if (nins.noneMatch(ins::contains)) {
                return this;
            } else {
                Set<N> b = begin, e = end;
                QualifiedSet<N, Vertex<N>> v = vertices;
                nins = nins.removeAll(ins);
                v = v.put(Vertex.of(node, nins, outs(node)));
                if (nins.isEmpty()) {
                    b = b.add(node);
                }
                for (N in : ins) {
                    Vertex<N> iv = v.get(in);
                    Set<N> outs = iv.outs().remove(node);
                    v.put(Vertex.of(in, iv.ins(), outs));
                    if (outs.isEmpty()) {
                        e = e.add(in);
                    }
                }
                return construct(b, e, v);
            }
        }
    }

}
