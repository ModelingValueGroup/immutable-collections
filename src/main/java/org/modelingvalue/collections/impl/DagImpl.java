//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//  (C) Copyright 2018-2024 Modeling Value Group B.V. (http://modelingvalue.org)                                         ~
//                                                                                                                       ~
//  Licensed under the GNU Lesser General Public License v3.0 (the 'License'). You may not use this file except in       ~
//  compliance with the License. You may obtain a copy of the License at: https://choosealicense.com/licenses/lgpl-3.0   ~
//  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on  ~
//  an 'AS IS' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the   ~
//  specific language governing permissions and limitations under the License.                                           ~
//                                                                                                                       ~
//  Maintainers:                                                                                                         ~
//      Wim Bast, Tom Brus                                                                                               ~
//                                                                                                                       ~
//  Contributors:                                                                                                        ~
//      Ronald Krijgsheld ✝, Arjan Kok, Carel Bast                                                                       ~
// --------------------------------------------------------------------------------------------------------------------- ~
//  In Memory of Ronald Krijgsheld, 1972 - 2023                                                                          ~
//      Ronald was suddenly and unexpectedly taken from us. He was not only our long-term colleague and team member      ~
//      but also our friend. "He will live on in many of the lines of code you see below."                               ~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

package org.modelingvalue.collections.impl;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.Dag;
import org.modelingvalue.collections.List;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.Vertex;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.TriFunction;

public class DagImpl<N> extends CollectionImpl<Vertex<N>> implements Dag<N> {
    private static final long                serialVersionUID = -1977417266823883798L;

    private final Set<N>                     begin;
    private final Set<N>                     end;
    private final QualifiedSet<N, Vertex<N>> vertices;

    public DagImpl(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        this.begin = begin;
        this.end = end;
        this.vertices = vertices;
    }

    private DagImpl(Set<N>[] beginEnd, QualifiedSet<N, Vertex<N>> vertices) {
        this(beginEnd[0], beginEnd[1], vertices);
    }

    protected Dag<N> construct(Set<N>[] beginEnd, QualifiedSet<N, Vertex<N>> vertices) {
        return vertices.isEmpty() ? Dag.of() : new DagImpl<N>(beginEnd, vertices);
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

    private static <E> QualifiedSet<E, Vertex<E>> put(QualifiedSet<E, Vertex<E>> v, E n, Set<E> i, Set<E> o, Set<E>[] be) {
        Set<E> pi = ins(v, n);
        Set<E> po = outs(v, n);
        if (i.isEmpty() && o.isEmpty()) {
            be[0] = be[0].remove(n);
            be[1] = be[1].remove(n);
            v = v.removeKey(n);
        } else {
            be[0] = i.isEmpty() ? be[0].add(n) : be[0].remove(n);
            be[1] = o.isEmpty() ? be[1].add(n) : be[1].remove(n);
            v = v.put(Vertex.of(n, i, o));
        }
        for (E in : pi) {
            if (!i.contains(in)) {
                v = put(v, in, ins(v, in), outs(v, in).remove(n), be);
            }
        }
        for (E in : i) {
            if (!pi.contains(in)) {
                v = put(v, in, ins(v, in), outs(v, in).add(n), be);
            }
        }
        for (E out : po) {
            if (!o.contains(out)) {
                v = put(v, out, ins(v, out).remove(n), outs(v, out), be);
            }
        }
        for (E out : o) {
            if (!po.contains(out)) {
                v = put(v, out, ins(v, out).add(n), outs(v, out), be);
            }
        }
        return v;
    }

    @SuppressWarnings("unchecked")
    private Set<N>[] beginEnd() {
        return new Set[]{begin, end};
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Dag<N> removeCycles() {
        Set<Pair<N, N>> cycles = cycles();
        if (cycles.isEmpty()) {
            return this instanceof Dag ? (Dag) this : new DagImpl<N>(begin, end, vertices);
        } else {
            Set<N>[] be = beginEnd();
            QualifiedSet<N, Vertex<N>> v = vertices;
            for (Pair<N, N> c : cycles) {
                v = put(v, c.a(), ins(v, c.a()), outs(v, c.a()).remove(c.b()), be);
            }
            return new DagImpl<N>(be[0], be[1], v);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<N> topological() {
        return dfs(List.of(), (l, n, c) -> c != null ? l : l.add(n));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Dag<N> prune(N node) {
        Vertex<N> vertex = vertices.get(node);
        if (vertex != null) {
            Set<N>[] beginEnd = beginEnd();
            return construct(beginEnd, prune(vertices, vertex, beginEnd));
        } else {
            return this;
        }
    }

    private static <E> QualifiedSet<E, Vertex<E>> prune(QualifiedSet<E, Vertex<E>> vertices, Vertex<E> vertex, Set<E>[] beginEnd) {
        vertices = put(vertices, vertex.node(), Set.of(), Set.of(), beginEnd);
        for (E out : vertex.outs()) {
            Vertex<E> vout = vertices.get(out);
            if (vout != null && vout.ins().isEmpty()) {
                vertices = prune(vertices, vout, beginEnd);
            }
        }
        return vertices;
    }

    @Override
    public Set<N> ins(N node) {
        return ins(vertices, node);
    }

    @Override
    public Set<N> outs(N node) {
        return outs(vertices, node);
    }

    private static <E> Set<E> ins(QualifiedSet<E, Vertex<E>> v, E n) {
        Vertex<E> nv = v.get(n);
        return nv != null ? nv.ins() : Set.of();
    }

    private static <E> Set<E> outs(QualifiedSet<E, Vertex<E>> v, E n) {
        Vertex<E> nv = v.get(n);
        return nv != null ? nv.outs() : Set.of();
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
    public Collection<N> nodes() {
        return vertices.toKeys();
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
            DagImpl<?> other = (DagImpl<?>) obj;
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
        return Dag.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> merge(Dag<N>[] branches, int length) {
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
        return new DagImpl<N>(mb.filter(b -> ins(mv, b).isEmpty()).asSet(), me.filter(e -> outs(mv, e).isEmpty()).asSet(), mv);
    }

    @Override
    public Dag<N> getMerger() {
        return Dag.of();
    }

    @Override
    public boolean containsEdge(N from, N to) {
        return outs(from).contains(to);
    }

    @Override
    public boolean containsNode(N node) {
        return vertices.containsKey(node);
    }

    @Override
    public Dag<N> put(N node, Set<N> ins, Set<N> outs) {
        Set<N> is = ins(node);
        Set<N> os = outs(node);
        return remove(node, is.removeAll(ins), os.removeAll(outs)).add(node, ins.removeAll(is), outs.removeAll(os));
    }

    @Override
    public Dag<N> putOuts(N node, Set<N> outs) {
        Set<N> os = outs(node);
        return removeOuts(node, os.removeAll(outs)).addOuts(node, outs.removeAll(os));
    }

    @Override
    public Dag<N> putIns(N node, Set<N> ins) {
        Set<N> is = ins(node);
        return removeIns(node, is.removeAll(ins)).addIns(node, ins.removeAll(is));
    }

    @Override
    public Dag<N> add(N node, Set<N> ins, Set<N> outs) {
        return addIns(node, ins).addOuts(node, outs);
    }

    @Override
    public Dag<N> clear(N node) {
        return remove(node, ins(node), outs(node));
    }

    @Override
    public Dag<N> clearOuts(N node) {
        return removeOuts(node, outs(node));
    }

    @Override
    public Dag<N> clearIns(N node) {
        return removeIns(node, ins(node));
    }

    @Override
    public Dag<N> remove(N node, Set<N> ins, Set<N> outs) {
        return removeIns(node, ins).removeOuts(node, outs);
    }

    @Override
    public Dag<N> addEdge(N from, N to) {
        Set<N> fouts = outs(from);
        if (fouts.contains(to)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            return new DagImpl<N>(be, put(vertices, from, ins(from), fouts.add(to), be));
        }
    }

    @Override
    public Dag<N> removeEdge(N from, N to) {
        Set<N> fouts = outs(from);
        if (fouts.contains(to)) {
            Set<N>[] be = beginEnd();
            return construct(be, put(vertices, from, ins(from), fouts.remove(to), be));
        } else {
            return this;
        }
    }

    @Override
    public Dag<N> addOuts(N node, Set<N> outs) {
        if (outs.isEmpty()) {
            return this;
        } else {
            Set<N> nouts = outs(node);
            if (nouts.containsAll(outs)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                return new DagImpl<N>(be, put(vertices, node, ins(node), nouts.addAll(outs), be));
            }
        }
    }

    @Override
    public Dag<N> addIns(N node, Set<N> ins) {
        if (ins.isEmpty()) {
            return this;
        } else {
            Set<N> nins = ins(node);
            if (nins.containsAll(ins)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                return new DagImpl<N>(be, put(vertices, node, nins.addAll(ins), outs(node), be));
            }
        }
    }

    @Override
    public Dag<N> removeOuts(N node, Set<N> outs) {
        if (outs.isEmpty()) {
            return this;
        } else {
            Set<N> nouts = outs(node);
            if (nouts.noneMatch(outs::contains)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                return construct(be, put(vertices, node, ins(node), nouts.removeAll(outs), be));
            }
        }
    }

    @Override
    public Dag<N> removeIns(N node, Set<N> ins) {
        if (ins.isEmpty()) {
            return this;
        } else {
            Set<N> nins = ins(node);
            if (nins.noneMatch(ins::contains)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                return construct(be, put(vertices, node, nins.removeAll(ins), outs(node), be));
            }
        }
    }

}
