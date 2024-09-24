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
import org.modelingvalue.collections.util.SerializableFunction;
import org.modelingvalue.collections.util.TriConsumer;
import org.modelingvalue.collections.util.TriFunction;

public class DagImpl<N> extends CollectionImpl<Vertex<N>> implements Dag<N> {
    private static final long                                 serialVersionUID = -1977417266823883798L;

    @SuppressWarnings("rawtypes")
    private static final SerializableFunction<Vertex, Object> NODE_OF_VERTEX   = Vertex::node;
    @SuppressWarnings("rawtypes")
    private static final QualifiedSet                         EMPTY_VERTICES   = QualifiedSet.of(NODE_OF_VERTEX);

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static final Dag                                   EMPTY            = new DagImpl(Set.of(), Set.of(), EMPTY_VERTICES);

    private final Set<N>                                      begin;
    private final Set<N>                                      end;
    private final QualifiedSet<N, Vertex<N>>                  vertices;

    @SuppressWarnings("unchecked")
    public DagImpl(N[] edges) {
        this(new Set[]{Set.of(), Set.of()}, edges);
    }

    @SuppressWarnings("unchecked")
    private DagImpl(Set<N>[] beginEnd, N[] edges) {
        this(beginEnd, addEdges(EMPTY_VERTICES, edges, beginEnd));
    }

    private DagImpl(Set<N>[] beginEnd, QualifiedSet<N, Vertex<N>> vertices) {
        this(beginEnd[0], beginEnd[1], vertices);
    }

    private DagImpl(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        this.begin = begin;
        this.end = end;
        this.vertices = vertices;
    }

    @Override
    public <A> A dfs(A acc, TriFunction<A, N, N, A> func, boolean frwrd) {
        return dfs(vertices, begin, end, acc, func, frwrd);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<N> topological() {
        return dfs(List.of(), (l, n, c) -> c != null ? l : l.prepend(n), true);
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
    public Collection<N> nodes() {
        return vertices.toKeys();
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
    public Class<?> getMeetClass() {
        return Dag.class;
    }

    @Override
    public Dag<N> getMerger() {
        return Dag.of();
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
        return new DagImpl<N>(mb.filter(b -> ins(mv.get(b)).isEmpty()).asSet(), me.filter(e -> outs(mv.get(e)).isEmpty()).asSet(), mv);
    }

    // change methods

    @Override
    public Dag<N> put(N node, Set<N> ins, Set<N> outs) {
        Vertex<N> v = vertices.get(node);
        Set<N> is = ins(v);
        Set<N> os = outs(v);
        if (is.equals(ins) && os.equals(outs)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            return construct(be, put(vertices, node, is, os, ins, outs, be));
        }
    }

    @Override
    public Dag<N> putOuts(N node, Set<N> outs) {
        Vertex<N> v = vertices.get(node);
        Set<N> os = outs(v);
        if (os.equals(outs)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> is = ins(v);
            return construct(be, put(vertices, node, is, os, is, outs, be));
        }
    }

    @Override
    public Dag<N> putIns(N node, Set<N> ins) {
        Vertex<N> v = vertices.get(node);
        Set<N> is = ins(v);
        if (is.equals(ins)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> os = outs(v);
            return construct(be, put(vertices, node, is, os, ins, os, be));
        }
    }

    @Override
    public Dag<N> clear(N node) {
        Vertex<N> v = vertices.get(node);
        Set<N> os = outs(v);
        Set<N> is = ins(v);
        if (os.isEmpty() && is.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            return construct(be, put(vertices, node, is, os, Set.of(), Set.of(), be));
        }
    }

    @Override
    public Dag<N> clearOuts(N node) {
        Vertex<N> v = vertices.get(node);
        Set<N> os = outs(v);
        if (os.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> is = ins(v);
            return construct(be, put(vertices, node, is, os, is, Set.of(), be));
        }
    }

    @Override
    public Dag<N> clearIns(N node) {
        Vertex<N> v = vertices.get(node);
        Set<N> is = ins(v);
        if (is.isEmpty()) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> os = outs(v);
            return construct(be, put(vertices, node, is, os, Set.of(), os, be));
        }
    }

    @Override
    public Dag<N> addEdge(N from, N to) {
        Vertex<N> v = vertices.get(from);
        Set<N> os = outs(v);
        if (os.contains(to)) {
            return this;
        } else {
            Set<N>[] be = beginEnd();
            Set<N> is = ins(v);
            return construct(be, put(vertices, from, is, os, is, os.add(to), be));
        }
    }

    @Override
    public Dag<N> removeEdge(N from, N to) {
        Vertex<N> v = vertices.get(from);
        Set<N> os = outs(v);
        if (os.contains(to)) {
            Set<N>[] be = beginEnd();
            Set<N> is = ins(v);
            return construct(be, put(vertices, from, is, os, is, os.remove(to), be));
        } else {
            return this;
        }
    }

    @Override
    public Dag<N> add(N node, Set<N> ins, Set<N> outs) {
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
                return construct(be, put(vertices, node, is, os, is.addAll(ins), os.addAll(outs), be));
            }
        }
    }

    @Override
    public Dag<N> addOuts(N node, Set<N> outs) {
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
                return construct(be, put(vertices, node, is, os, is, os.addAll(outs), be));
            }
        }
    }

    @Override
    public Dag<N> addIns(N node, Set<N> ins) {
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
                return construct(be, put(vertices, node, nins, nouts, nins.addAll(ins), nouts, be));
            }
        }
    }

    @Override
    public Dag<N> remove(N node, Set<N> ins, Set<N> outs) {
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
                return construct(be, put(vertices, node, is, os, is.removeAll(ins), os.removeAll(outs), be));
            }
        }
    }

    @Override
    public Dag<N> removeOuts(N node, Set<N> outs) {
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
                return construct(be, put(vertices, node, is, os, is, os.removeAll(outs), be));
            }
        }
    }

    @Override
    public Dag<N> removeIns(N node, Set<N> ins) {
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
                return construct(be, put(vertices, node, is, os, is.removeAll(ins), os, be));
            }
        }
    }

    // private utility methods

    @SuppressWarnings("unchecked")
    private Set<N>[] beginEnd() {
        return new Set[]{begin, end};
    }

    private Dag<N> construct(Set<N>[] beginEnd, QualifiedSet<N, Vertex<N>> vertices) {
        return vertices.isEmpty() ? Dag.of() : this.vertices.equals(vertices) ? this : new DagImpl<N>(beginEnd, vertices);
    }

    private static <E> Set<E> ins(Vertex<E> v) {
        return v != null ? v.ins() : Set.of();
    }

    private static <E> Set<E> outs(Vertex<E> v) {
        return v != null ? v.outs() : Set.of();
    }

    private static <E> QualifiedSet<E, Vertex<E>> addEdges(QualifiedSet<E, Vertex<E>> vs, E[] edges, Set<E>[] be) {
        int l = edges.length;
        if (l == 0) {
            throw new IllegalArgumentException("Edges lengths should be greater than 0, is " + l);
        } else if (l % 2 != 0) {
            throw new IllegalArgumentException("Edges lengths should be divisible by 2, is " + l);
        }
        for (int i = 0; i < l; i += 2) {
            Vertex<E> v = vs.get(edges[i]);
            Set<E> ins = ins(v);
            Set<E> outs = outs(v);
            vs = put(vs, edges[i], ins, outs, ins, outs.add(edges[i + 1]), be);
        }
        return vs;
    }

    @SuppressWarnings("unchecked")
    private static <E, A> A dfs(QualifiedSet<E, Vertex<E>> vs, Set<E> b, Set<E> e, A acc, TriFunction<A, E, E, A> func, boolean frwrd) {
        int size = vs.size();
        BitSet temp = new BitSet(size), perm = new BitSet(size);
        for (E n : frwrd ? b : e) {
            acc = visit(vs, acc, n, null, func, temp, perm, frwrd);
        }
        return acc;
    }

    private static <E, A> A visit(QualifiedSet<E, Vertex<E>> vs, A a, E n, E c, TriFunction<A, E, E, A> f, BitSet temp, BitSet perm, boolean frwrd) {
        Vertex<E> v = vs.get(n);
        int i = vs.index(v);
        if (perm.get(i)) {
            return a;
        } else if (temp.get(i)) {
            // cycle
            assert c != null;
            return f.apply(a, n, c);
        } else {
            temp.set(i);
            for (E o : frwrd ? v.outs() : v.ins()) {
                a = visit(vs, a, o, n, f, temp, perm, frwrd);
            }
            perm.set(i);
            return f.apply(a, v.node(), null);
        }
    }

    private static <E> QualifiedSet<E, Vertex<E>> put(QualifiedSet<E, Vertex<E>> vs, E n, Set<E> pi, Set<E> po, Set<E> ni, Set<E> no, Set<E>[] be) {
        if (!pi.equals(ni) || !po.equals(no)) {
            if (ni.isEmpty() && no.isEmpty()) {
                be[0] = be[0].remove(n);
                be[1] = be[1].remove(n);
                vs = vs.removeKey(n);
            } else {
                be[0] = ni.isEmpty() ? be[0].add(n) : be[0].remove(n);
                be[1] = no.isEmpty() ? be[1].add(n) : be[1].remove(n);
                vs = vs.put(Vertex.of(n, ni, no));
            }
            for (E in : pi) {
                if (!ni.contains(in)) {
                    Vertex<E> v = vs.get(in);
                    Set<E> ins = ins(v);
                    Set<E> outs = outs(v);
                    vs = put(vs, in, ins, outs, ins, outs.remove(n), be);
                }
            }
            for (E in : ni) {
                if (!pi.contains(in)) {
                    Vertex<E> v = vs.get(in);
                    Set<E> ins = ins(v);
                    Set<E> outs = outs(v);
                    vs = put(vs, in, ins, outs, ins, outs.add(n), be);
                }
            }
            for (E out : po) {
                if (!no.contains(out)) {
                    Vertex<E> v = vs.get(out);
                    Set<E> ins = ins(v);
                    Set<E> outs = outs(v);
                    vs = put(vs, out, ins, outs, ins.remove(n), outs, be);
                }
            }
            for (E out : no) {
                if (!po.contains(out)) {
                    Vertex<E> v = vs.get(out);
                    Set<E> ins = ins(v);
                    Set<E> outs = outs(v);
                    vs = put(vs, out, ins, outs, ins.add(n), outs, be);
                }
            }
        }
        return vs;
    }

}
