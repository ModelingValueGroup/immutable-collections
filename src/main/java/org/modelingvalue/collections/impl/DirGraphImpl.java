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

import java.util.BitSet;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.modelingvalue.collections.*;
import org.modelingvalue.collections.util.Deserializer;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadFunction;
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.Serializer;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.TriFunction;

public class DirGraphImpl<N> extends CollectionImpl<Vertex<N>> implements DirGraph<N> {
    private static final long                                 serialVersionUID = -1977417266823883798L;

    @SuppressWarnings("rawtypes")
    private static final SerializableFunction<Vertex, Object> NODE_OF_VERTEX   = Vertex::node;
    @SuppressWarnings("rawtypes")
    private static final QualifiedSet                         EMPTY_VERTICES   = QualifiedSet.of(NODE_OF_VERTEX);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final Dag                                   EMPTY            = new DagImpl(Set.of(), Set.of(), EMPTY_VERTICES);

    transient private Set<N>                                  begin;
    transient private Set<N>                                  end;
    transient private QualifiedSet<N, Vertex<N>>              vertices;

    @SuppressWarnings("unchecked")
    public DirGraphImpl(N[] edges) {
        this(new Set[]{Set.of(), Set.of()}, edges);
    }

    @SuppressWarnings("unchecked")
    private QualifiedSet<N, Vertex<N>> emptyVertices() {
        return EMPTY_VERTICES;
    }

    @SuppressWarnings("unchecked")
    private DirGraphImpl(Set<N>[] beginEnd, N[] edges) {
        this(beginEnd, addEdges(EMPTY_VERTICES, edges, beginEnd));
    }

    private DirGraphImpl(Set<N>[] beginEnd, QualifiedSet<N, Vertex<N>> vertices) {
        this(beginEnd[0], beginEnd[1], vertices);
    }

    protected DirGraphImpl(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        this.begin = begin;
        this.end = end;
        this.vertices = vertices;
    }

    protected DirGraph<N> construct(Set<N>[] beginEnd, QualifiedSet<N, Vertex<N>> vertices, boolean possibleCycleAdded) {
        return vertices.isEmpty() ? DirGraph.of() : this.vertices.equals(vertices) ? this : new DirGraphImpl<N>(beginEnd, vertices);
    }

    @Override
    public <A> A dfsNodes(A acc, BiFunction<A, N, A> func) {
        return dfsNodes(vertices, Collection.concat(begin(), nodes()), acc, func, true);
    }

    @Override
    public <A> A invDfsNodes(A acc, BiFunction<A, N, A> func) {
        return dfsNodes(vertices, Collection.concat(end(), nodes()), acc, func, false);
    }

    @Override
    public <A> A dfsEdges(A acc, QuadFunction<A, N, N, Boolean, A> func) {
        return dfsEdges(vertices, Collection.concat(begin(), nodes()), acc, func, true);
    }

    @Override
    public <A> A invDfsEdges(A acc, QuadFunction<A, N, N, Boolean, A> func) {
        return dfsEdges(vertices, Collection.concat(end(), nodes()), acc, func, false);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<N> topologicalNodes() {
        return dfsNodes(List.of(), (l, n) -> l.prepend(n));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<N> invTopologicalNodes() {
        return invDfsNodes(List.of(), (l, n) -> l.prepend(n));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Pair<N, N>> topologicalEdges() {
        return dfsEdges(List.of(), (l, f, t, c) -> l.prepend(Pair.of(f, t)));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<Pair<N, N>> invTopologicalEdges() {
        return invDfsEdges(List.of(), (l, t, f, c) -> l.prepend(Pair.of(f, t)));
    }

    @Override
    public Set<Pair<N, N>> cycles() {
        return dfsEdges(Set.of(), (cs, f, t, c) -> c ? cs.add(Pair.of(f, t)) : cs);
    }

    @Override
    public Set<Pair<N, N>> invCycles() {
        return invDfsEdges(Set.of(), (cs, t, f, c) -> c ? cs.add(Pair.of(f, t)) : cs);
    }

    @Override
    public Set<N> ins(N node) {
        return ins(vertices.get(node));
    }

    @Override
    public Set<N> outs(N node) {
        return outs(vertices.get(node));
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
    public Vertex<N> vertex(N node) {
        return vertices.get(node);
    }

    @Override
    public Collection<N> nodes() {
        return vertices.toKeys();
    }

    @Override
    public Collection<Pair<N, N>> edges() {
        return vertices.flatMap(v -> v.outs().map(o -> Pair.of(v.node(), o)));
    }

    @Override
    public boolean contains(Object object) {
        return vertices.contains(object);
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
    public Class<?> getMeetClass() {
        return DirGraph.class;
    }

    @Override
    public DirGraph<N> getMerger() {
        return DirGraph.of();
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
        Set<N> mb = begin.merge(ba);
        Set<N> me = end.merge(ea);
        QualifiedSet<N, Vertex<N>> mv = vertices.merge(va);
        Set<N>[] be = new Set[]{mb.filter(b -> ins(mv.get(b)).isEmpty()).asSet(), me.filter(b -> outs(mv.get(b)).isEmpty()).asSet()};
        return new DirGraphImpl<N>(be, mv);
    }

    @Override
    public Set<N> connected() {
        return dfsNodes(vertices, begin, Set.of(), (s, n) -> s.add(n), true);
    }

    @Override
    public Set<N> invConnected() {
        return dfsNodes(vertices, end, Set.of(), (s, n) -> s.add(n), false);
    }

    // change methods

    @Override
    public DirGraph<N> addEdges(Set<Pair<N, N>> edges) {
        if (edges.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            QualifiedSet<N, Vertex<N>> vs = vertices;
            for (Pair<N, N> edge : edges) {
                Vertex<N> v = vs.get(edge.a());
                Set<N> os = outs(v);
                if (!os.contains(edge.b())) {
                    Set<N> is = ins(v);
                    vs = put(vs, edge.a(), is, os, is, os.add(edge.b()), be);
                }
            }
            return construct(be, vs, true);
        }
    }

    @Override
    public DirGraph<N> removeEdges(Set<Pair<N, N>> edges) {
        if (edges.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            QualifiedSet<N, Vertex<N>> vs = vertices;
            for (Pair<N, N> edge : edges) {
                Vertex<N> v = vs.get(edge.a());
                if (v != null) {
                    Set<N> os = outs(v);
                    if (os.contains(edge.b())) {
                        Set<N> is = ins(v);
                        vs = put(vs, edge.a(), is, os, is, os.remove(edge.b()), be);
                    }
                }
            }
            return construct(be, vs, false);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> removeDisconnected() {
        return retainNodes(connected());
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> invRemoveDisconnected() {
        return retainNodes(invConnected());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> removeCycles() {
        return removeCycles(cycles());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> invRemoveCycles() {
        return removeCycles(invCycles());
    }

    private Dag<N> removeCycles(Set<Pair<N, N>> cycles) {
        Set<N>[] be = beginEnd();
        QualifiedSet<N, Vertex<N>> vs = vertices;
        for (Pair<N, N> edge : cycles) {
            Vertex<N> v = vs.get(edge.a());
            Set<N> os = outs(v);
            Set<N> is = ins(v);
            vs = put(vs, edge.a(), is, os, is, os.remove(edge.b()), be);
        }
        return new DagImpl<N>(be, vs);
    }

    @Override
    public DirGraph<N> removeNodes(Set<N> nodes) {
        if (nodes.isEmpty()) {
            return this;
        } else {
            QualifiedSet<N, Vertex<N>> vs = vertices;
            Set<N>[] be = beginEnd();
            for (N n : nodes) {
                Vertex<N> v = vs.get(n);
                if (v != null) {
                    Set<N> os = outs(v);
                    Set<N> is = ins(v);
                    vs = put(vs, n, is, os, Set.of(), Set.of(), be);
                }
            }
            return construct(be, vs, false);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> retainNodes(Set<N> nodes) {
        if (nodes.isEmpty()) {
            return EMPTY;
        } else {
            QualifiedSet<N, Vertex<N>> vs = emptyVertices();
            Set<N>[] be = new Set[]{Set.of(), Set.of()};
            for (N n : nodes) {
                Vertex<N> v = vertex(n);
                if (v != null) {
                    Set<N> ins = ins(v).retainAll(nodes);
                    Set<N> outs = outs(v).retainAll(nodes);
                    if (!ins.isEmpty() || !outs.isEmpty()) {
                        if (ins.isEmpty()) {
                            be[0] = be[0].add(n);
                        }
                        if (outs.isEmpty()) {
                            be[1] = be[1].add(n);
                        }
                        vs = vs.put(v.ins().equals(ins) && v.outs().equals(outs) ? v : Vertex.of(n, ins, outs));
                    }
                }
            }
            return construct(be, vs, false);
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> removeNodes(N... nodes) {
        return removeNodes(Set.of(nodes));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> retainNodes(N... nodes) {
        return retainNodes(Set.of(nodes));
    }

    @Override
    public DirGraph<N> setBegin(Set<N> begin) {
        Set<N> rs = begin().removeAll(begin);
        Set<N> as = begin.removeAll(begin()).filter(this::containsNode).asSet();
        if (rs.isEmpty() && as.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            QualifiedSet<N, Vertex<N>> vs = vertices;
            for (N n : as) {
                Vertex<N> v = vs.get(n);
                vs = put(vs, n, ins(v), outs(v), Set.of(), outs(v), be);
            }
            do {
                for (N n : rs) {
                    vs = put(vs, n, Set.of(), outs(vs.get(n)), Set.of(), Set.of(), be);
                }
                rs = be[0].removeAll(begin);
            } while (!rs.isEmpty());
            return construct(be, vs, false);
        }
    }

    @Override
    public DirGraph<N> setEnd(Set<N> end) {
        Set<N> rs = end().removeAll(end);
        Set<N> as = end.removeAll(end()).filter(this::containsNode).asSet();
        if (rs.isEmpty() && as.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            QualifiedSet<N, Vertex<N>> vs = vertices;
            for (N n : as) {
                Vertex<N> v = vs.get(n);
                vs = put(vs, n, ins(v), outs(v), ins(v), Set.of(), be);
            }
            do {
                for (N n : rs) {
                    vs = put(vs, n, ins(vs.get(n)), Set.of(), Set.of(), Set.of(), be);
                }
                rs = be[1].removeAll(end);
            } while (!rs.isEmpty());
            return construct(be, vs, false);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> setBegin(N... begin) {
        return setBegin(Set.of(begin));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> setEnd(N... end) {
        return setEnd(Set.of(end));
    }

    @Override
    public DirGraph<N> putBegin(N node, Set<N> outs) {
        return put(node, Set.of(), outs);
    }

    @Override
    public DirGraph<N> putEnd(N node, Set<N> ins) {
        return put(node, ins, Set.of());
    }

    @Override
    public DirGraph<N> put(N node, Set<N> ins, Set<N> outs) {
        ins = ins.remove(node);
        outs = outs.remove(node);
        Vertex<N> v = vertices.get(node);
        Set<N> is = ins(v);
        Set<N> os = outs(v);
        if (is.equals(ins) && os.equals(outs)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            return construct(be, put(vertices, node, is, os, ins, outs, be), true);
        }
    }

    @Override
    public DirGraph<N> putOuts(N node, Set<N> outs) {
        outs = outs.remove(node);
        Vertex<N> v = vertices.get(node);
        Set<N> os = outs(v);
        if (os.equals(outs)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> is = ins(v);
            return construct(be, put(vertices, node, is, os, is, outs, be), true);
        }
    }

    @Override
    public DirGraph<N> putIns(N node, Set<N> ins) {
        ins = ins.remove(node);
        Vertex<N> v = vertices.get(node);
        Set<N> is = ins(v);
        if (is.equals(ins)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> os = outs(v);
            return construct(be, put(vertices, node, is, os, ins, os, be), true);
        }
    }

    @Override
    public DirGraph<N> clear(N node) {
        Vertex<N> v = vertices.get(node);
        Set<N> os = outs(v);
        Set<N> is = ins(v);
        if (os.isEmpty() && is.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            return construct(be, put(vertices, node, is, os, Set.of(), Set.of(), be), false);
        }
    }

    @Override
    public DirGraph<N> clearOuts(N node) {
        Vertex<N> v = vertices.get(node);
        Set<N> os = outs(v);
        if (os.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> is = ins(v);
            return construct(be, put(vertices, node, is, os, is, Set.of(), be), false);
        }
    }

    @Override
    public DirGraph<N> clearIns(N node) {
        Vertex<N> v = vertices.get(node);
        Set<N> is = ins(v);
        if (is.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> os = outs(v);
            return construct(be, put(vertices, node, is, os, Set.of(), os, be), false);
        }
    }

    @Override
    public DirGraph<N> addEdge(N from, N to) {
        Vertex<N> v = vertices.get(from);
        Set<N> os = outs(v);
        if (os.contains(to)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> is = ins(v);
            return construct(be, put(vertices, from, is, os, is, os.add(to), be), true);
        }
    }

    @Override
    public DirGraph<N> removeEdge(N from, N to) {
        Vertex<N> v = vertices.get(from);
        Set<N> os = outs(v);
        if (os.contains(to)) {
            Set<N>[] be = beginEnd();
            Set<N> is = ins(v);
            return construct(be, put(vertices, from, is, os, is, os.remove(to), be), false);
        } else {
            return this;
        }
    }

    @Override
    public DirGraph<N> add(N node, Set<N> ins, Set<N> outs) {
        ins = ins.remove(node);
        outs = outs.remove(node);
        if (ins.isEmpty() && outs.isEmpty()) {
            return this;
        } else {
            Vertex<N> v = vertices.get(node);
            Set<N> is = ins(v);
            Set<N> os = outs(v);
            if (is.containsAll(ins) && os.containsAll(outs)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                return construct(be, put(vertices, node, is, os, is.addAll(ins), os.addAll(outs), be), true);
            }
        }
    }

    @Override
    public DirGraph<N> addOuts(N node, Set<N> outs) {
        outs = outs.remove(node);
        if (outs.isEmpty()) {
            return this;
        } else {
            Vertex<N> v = vertices.get(node);
            Set<N> os = outs(v);
            if (os.containsAll(outs)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                Set<N> is = ins(v);
                return construct(be, put(vertices, node, is, os, is, os.addAll(outs), be), true);
            }
        }
    }

    @Override
    public DirGraph<N> addIns(N node, Set<N> ins) {
        ins = ins.remove(node);
        if (ins.isEmpty()) {
            return this;
        } else {
            Vertex<N> v = vertices.get(node);
            Set<N> nins = ins(v);
            if (nins.containsAll(ins)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                Set<N> nouts = outs(v);
                return construct(be, put(vertices, node, nins, nouts, nins.addAll(ins), nouts, be), true);
            }
        }
    }

    @Override
    public DirGraph<N> remove(N node, Set<N> ins, Set<N> outs) {
        ins = ins.remove(node);
        outs = outs.remove(node);
        if (outs.isEmpty() && ins.isEmpty()) {
            return this;
        } else {
            Vertex<N> v = vertices.get(node);
            Set<N> os = outs(v);
            Set<N> is = ins(v);
            if (os.noneMatch(outs::contains) && is.noneMatch(ins::contains)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                return construct(be, put(vertices, node, is, os, is.removeAll(ins), os.removeAll(outs), be), false);
            }
        }
    }

    @Override
    public DirGraph<N> removeOuts(N node, Set<N> outs) {
        outs = outs.remove(node);
        if (outs.isEmpty()) {
            return this;
        } else {
            Vertex<N> v = vertices.get(node);
            Set<N> os = outs(v);
            if (os.noneMatch(outs::contains)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                Set<N> is = ins(v);
                return construct(be, put(vertices, node, is, os, is, os.removeAll(outs), be), false);
            }
        }
    }

    @Override
    public DirGraph<N> removeIns(N node, Set<N> ins) {
        ins = ins.remove(node);
        if (ins.isEmpty()) {
            return this;
        } else {
            Vertex<N> v = vertices.get(node);
            Set<N> is = ins(v);
            if (is.noneMatch(ins::contains)) {
                return this;
            } else {
                Set<N>[] be = beginEnd();
                Set<N> os = outs(v);
                return construct(be, put(vertices, node, is, os, is.removeAll(ins), os, be), false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> addEdges(N... edges) {
        if (edges.length == 0) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            return construct(be, addEdges(vertices, edges, be), true);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> removeEdges(N... edges) {
        if (edges.length == 0) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            return construct(be, removeEdges(vertices, edges, be), false);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> putBegin(N node, N... outs) {
        return putBegin(node, Set.of(outs));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> putEnd(N node, N... ins) {
        return putEnd(node, Set.of(ins));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> putOuts(N node, N... outs) {
        return putOuts(node, Set.of(outs));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> putIns(N node, N... ins) {
        return putIns(node, Set.of(ins));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> addOuts(N node, N... outs) {
        return outs.length == 0 ? this : addOuts(node, Set.of(outs));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> addIns(N node, N... ins) {
        return ins.length == 0 ? this : addIns(node, Set.of(ins));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> removeOuts(N node, N... outs) {
        return outs.length == 0 ? this : removeOuts(node, Set.of(outs));
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> removeIns(N node, N... ins) {
        return ins.length == 0 ? this : removeIns(node, Set.of(ins));
    }

    // private utility methods

    @Override
    @SuppressWarnings("unchecked")
    public Set<N>[] beginEnd() {
        return new Set[]{begin, end};
    }

    private static <E> Set<E> ins(Vertex<E> v) {
        return v != null ? v.ins() : Set.of();
    }

    private static <E> Set<E> outs(Vertex<E> v) {
        return v != null ? v.outs() : Set.of();
    }

    private static <E> QualifiedSet<E, Vertex<E>> addEdges(QualifiedSet<E, Vertex<E>> vs, E[] edges, Set<E>[] be) {
        int l = edges.length;
        if (l % 2 != 0) {
            throw new IllegalArgumentException("Edges lengths should be divisible by 2, is " + l);
        }
        for (int i = 0; i < l; i += 2) {
            Vertex<E> v = vs.get(edges[i]);
            Set<E> outs = outs(v);
            if (!outs.contains(edges[i + 1])) {
                Set<E> ins = ins(v);
                vs = put(vs, edges[i], ins, outs, ins, outs.add(edges[i + 1]), be);
            }
        }
        return vs;
    }

    private static <E> QualifiedSet<E, Vertex<E>> removeEdges(QualifiedSet<E, Vertex<E>> vs, E[] edges, Set<E>[] be) {
        int l = edges.length;
        if (l % 2 != 0) {
            throw new IllegalArgumentException("Edges lengths should be divisible by 2, is " + l);
        }
        for (int i = 0; i < l; i += 2) {
            Vertex<E> v = vs.get(edges[i]);
            Set<E> outs = outs(v);
            if (outs.contains(edges[i + 1])) {
                Set<E> ins = ins(v);
                vs = put(vs, edges[i], ins, outs, ins, outs.remove(edges[i + 1]), be);
            }
        }
        return vs;
    }

    protected final <A> A dfsNodes(QualifiedSet<N, Vertex<N>> vs, Collection<N> s, A acc, BiFunction<A, N, A> func, boolean frwrd) {
        return dfs(vs, s, acc, (a, f, t, c) -> func.apply(a, t), frwrd, false);
    }

    protected final <A> A dfsEdges(QualifiedSet<N, Vertex<N>> vs, Collection<N> s, A acc, QuadFunction<A, N, N, Boolean, A> func, boolean frwrd) {
        return dfs(vs, s, acc, func, frwrd, true);
    }

    protected boolean checkCycles() {
        return true;
    }

    private <A> A dfs(QualifiedSet<N, Vertex<N>> vs, Collection<N> s, A acc, QuadFunction<A, N, N, Boolean, A> func, boolean frwrd, boolean edges) {
        int size = vs.size();
        int[] done = new int[1];
        BitSet temp = edges && checkCycles() ? new BitSet(size) : null, perm = new BitSet(size);
        for (N n : s) {
            acc = dfs(vs, acc, null, n, func, temp, perm, frwrd, edges, done);
            if (done[0] >= size) {
                return acc;
            }
        }
        return acc;
    }

    private static <E, A> A dfs(QualifiedSet<E, Vertex<E>> vs, A a, E f, E t, QuadFunction<A, E, E, Boolean, A> func, BitSet temp, BitSet perm, boolean frwrd, boolean edges, int[] done) {
        Vertex<E> v = vs.get(t);
        int i = vs.index(v);
        if (perm.get(i)) {
            // already done
            if (edges) {
                a = func.apply(a, f, t, false);
            }
        } else if (temp != null && temp.get(i)) {
            // cycle
            if (edges) {
                assert f != null;
                a = func.apply(a, f, t, true);
            }
        } else {
            done[0]++;
            if (temp != null) {
                temp.set(i);
            } else {
                perm.set(i);
            }
            for (E o : frwrd ? v.outs() : v.ins()) {
                a = dfs(vs, a, t, o, func, temp, perm, frwrd, edges, done);
            }
            if (temp != null) {
                perm.set(i);
            }
            a = func.apply(a, f, t, false);
        }
        return a;
    }

    private static <E> QualifiedSet<E, Vertex<E>> put(QualifiedSet<E, Vertex<E>> vs, E n, Set<E> pi, Set<E> po, Set<E> ni, Set<E> no, Set<E>[] be) {
        vs = putVertex(vs, n, ni, no, be);
        for (E in : pi) {
            if (!ni.contains(in)) {
                Vertex<E> v = vs.get(in);
                vs = putVertex(vs, in, ins(v), outs(v).remove(n), be);
            }
        }
        for (E out : po) {
            if (!no.contains(out)) {
                Vertex<E> v = vs.get(out);
                vs = putVertex(vs, out, ins(v).remove(n), outs(v), be);
            }
        }
        for (E in : ni) {
            if (!pi.contains(in)) {
                Vertex<E> v = vs.get(in);
                vs = putVertex(vs, in, ins(v), outs(v).add(n), be);
            }
        }
        for (E out : no) {
            if (!po.contains(out)) {
                Vertex<E> v = vs.get(out);
                vs = putVertex(vs, out, ins(v).add(n), outs(v), be);
            }
        }
        return vs;
    }

    private static <E> QualifiedSet<E, Vertex<E>> putVertex(QualifiedSet<E, Vertex<E>> vs, E n, Set<E> ni, Set<E> no, Set<E>[] be) {
        if (ni.isEmpty() && no.isEmpty()) {
            be[0] = be[0].remove(n);
            be[1] = be[1].remove(n);
            return vs.removeKey(n);
        } else {
            be[0] = ni.isEmpty() ? be[0].add(n) : be[0].remove(n);
            be[1] = no.isEmpty() ? be[1].add(n) : be[1].remove(n);
            return vs.put(Vertex.of(n, ni, no));
        }
    }

    // ContainingCollection methods

    @Override
    public <R extends ContainingCollection<Vertex<N>>> StreamCollection<R[]> compare(R other) {
        return vertices.compare(other);
    }

    @Override
    public int index(Object obj) {
        return vertices.index(obj);
    }

    @Override
    public Vertex<N> get(int index) {
        return vertices.get(index);
    }

    @Override
    public Collection<Vertex<N>> reverse() {
        return vertices.reverse();
    }

    @Override
    public Spliterator<Vertex<N>> reverseSpliterator() {
        return vertices.reverseSpliterator();
    }

    @Override
    public ListIterator<Vertex<N>> listIterator() {
        return vertices.listIterator();
    }

    @Override
    public ListIterator<Vertex<N>> listIterator(int index) {
        return vertices.listIterator(index);
    }

    @Override
    public ListIterator<Vertex<N>> listIteratorAtEnd() {
        return vertices.listIteratorAtEnd();
    }

    @Override
    public DirGraph<N> add(Vertex<N> av) {
        QualifiedSet<N, Vertex<N>> vs = vertices;
        Vertex<N> ev = vs.get(av.node());
        Set<N> os = outs(av);
        Set<N> is = ins(ev);
        if (is.containsAll(av.ins()) && os.containsAll(av.outs())) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            return construct(be, put(vs, av.node(), is, os, is.addAll(av.ins()), os.addAll(av.outs()), be), true);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> addAll(Collection<? extends Vertex<N>> coll) {
        QualifiedSet<N, Vertex<N>> vs = vertices;
        Set<N>[] be = beginEnd();
        for (Vertex<N> av : coll) {
            Vertex<N> ev = vs.get(av.node());
            Set<N> ins = ins(ev);
            Set<N> outs = outs(ev);
            if (!ins.containsAll(av.ins()) || !outs.containsAll(av.outs())) {
                vs = put(vs, av.node(), ins, outs, ins.addAll(av.ins()), outs.addAll(av.outs()), be);
            }
        }
        return construct(be, vs, true);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> remove(Object obj) {
        if (obj instanceof Vertex) {
            Vertex<N> rv = (Vertex<N>) obj;
            QualifiedSet<N, Vertex<N>> vs = vertices;
            Vertex<N> ev = vs.get(rv.node());
            Set<N> os = outs(ev);
            Set<N> is = ins(ev);
            if (!os.isEmpty() || !is.isEmpty()) {
                Set<N>[] be = beginEnd();
                vs = put(vs, rv.node(), is, os, Set.of(), Set.of(), be);
                return construct(be, vs, false);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> removeAll(Collection<?> coll) {
        QualifiedSet<N, Vertex<N>> vs = vertices;
        Set<N>[] be = beginEnd();
        for (Object obj : coll) {
            if (obj instanceof Vertex) {
                Vertex<N> rv = (Vertex<N>) obj;
                Vertex<N> ev = vs.get(rv.node());
                Set<N> os = outs(ev);
                Set<N> is = ins(ev);
                if (!os.isEmpty() || !is.isEmpty()) {
                    vs = put(vs, rv.node(), is, os, Set.of(), Set.of(), be);
                }
            }
        }
        return construct(be, vs, false);
    }

    @Override
    public DirGraph<N> addUnique(Vertex<N> av) {
        return add(av);
    }

    @Override
    public DirGraph<N> addAllUnique(Collection<? extends Vertex<N>> coll) {
        return addAll(coll);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DirGraph<N> replace(Object pre, Vertex<N> post) {
        DirGraph<N> result = remove(pre);
        if (result != this) {
            result = result.add(post);
        }
        return result;
    }

    @Override
    public DirGraph<N> replaceFirst(Object pre, Vertex<N> post) {
        return replace(pre, post);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Dag<N> clear() {
        return EMPTY;
    }

    @Override
    public void javaSerialize(Serializer s) {
        s.writeInt(vertices.size());
        for (Object e : vertices) {
            s.writeObject(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void javaDeserialize(Deserializer s) {
        int size = s.readInt();
        Set<N> bs = Set.of();
        Set<N> es = Set.of();
        QualifiedSet<N, Vertex<N>> vs = emptyVertices();
        for (int i = 0; i < size; i++) {
            Vertex<N> v = s.readObject();
            vs = vs.add(v);
            if (v.ins().isEmpty()) {
                bs = bs.add(v.node());
            }
            if (v.outs().isEmpty()) {
                es = es.add(v.node());
            }
        }
        this.vertices = vs;
        this.begin = bs;
        this.end = es;
    }

}
