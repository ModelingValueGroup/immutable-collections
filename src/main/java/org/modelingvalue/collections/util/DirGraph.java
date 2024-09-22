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

package org.modelingvalue.collections.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.modelingvalue.collections.Collection;
import org.modelingvalue.collections.QualifiedSet;
import org.modelingvalue.collections.Set;
import org.modelingvalue.collections.impl.CollectionImpl;
import org.modelingvalue.collections.impl.StreamCollectionImpl;
import org.modelingvalue.collections.struct.impl.Struct3Impl;

public class DirGraph<N> extends CollectionImpl<DirGraph.Vertex<N>> implements Mergeable<DirGraph<N>> {
    private static final long                                 serialVersionUID = -1977417266823883798L;

    @SuppressWarnings("rawtypes")
    private static final SerializableFunction<Vertex, Object> NODE_OF_VERTEX   = Vertex::node;
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static final Dag                                  EMPTY            = new Dag(Set.of(), Set.of(), QualifiedSet.of(NODE_OF_VERTEX));

    @SuppressWarnings("unchecked")
    public static <E> Dag<E> empty() {
        return EMPTY;
    }

    protected final Set<N>                     begin;
    protected final Set<N>                     end;
    protected final QualifiedSet<N, Vertex<N>> vertices;

    protected DirGraph(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        this.begin = begin;
        this.end = end;
        this.vertices = vertices;
    }

    protected DirGraph<N> construct(Set<N> begin, Set<N> end, QualifiedSet<N, Vertex<N>> vertices) {
        return new DirGraph<N>(begin, end, vertices);
    }

    @SuppressWarnings("unchecked")
    public DirGraph<N> pruneOuts(N node) {
        Vertex<N> vertex = vertices.get(node);
        if (vertex != null) {
            Set<N>[] beginEnd = new Set[]{begin, end};
            QualifiedSet<N, Vertex<N>> pruned = pruneOuts(vertices, vertex, beginEnd);
            return construct(beginEnd[0], beginEnd[1], pruned);
        } else {
            return this;
        }
    }

    @SuppressWarnings("unchecked")
    public DirGraph<N> pruneIns(N node) {
        Vertex<N> vertex = vertices.get(node);
        if (vertex != null) {
            Set<N>[] beginEnd = new Set[]{begin, end};
            QualifiedSet<N, Vertex<N>> pruned = pruneIns(vertices, vertex, beginEnd);
            return construct(beginEnd[0], beginEnd[1], pruned);
        } else {
            return this;
        }
    }

    protected static <E> QualifiedSet<E, Vertex<E>> pruneOuts(QualifiedSet<E, Vertex<E>> vertices, Vertex<E> vertex, Set<E>[] beginEnd) {
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
                    pruned = pruneOuts(pruned, vout, beginEnd);
                } else {
                    Set<E> ins = vout.ins().remove(vertex.node());
                    pruned = pruned.put(Vertex.of(out, ins, vout.outs()));
                }
            }
        }
        return pruned;
    }

    protected static <E> QualifiedSet<E, Vertex<E>> pruneIns(QualifiedSet<E, Vertex<E>> vertices, Vertex<E> vertex, Set<E>[] beginEnd) {
        QualifiedSet<E, Vertex<E>> pruned = vertices.remove(vertex);
        if (vertex.outs().isEmpty()) {
            beginEnd[1] = beginEnd[1].remove(vertex.node());
        } else {
            for (E out : vertex.outs()) {
                Vertex<E> vout = vertices.get(out);
                Set<E> ins = vout.ins().remove(vertex.node());
                pruned = pruned.put(Vertex.of(out, ins, vout.outs()));
                if (ins.isEmpty()) {
                    beginEnd[0] = beginEnd[0].add(out);
                }
            }
        }
        if (vertex.ins().isEmpty()) {
            beginEnd[0] = beginEnd[0].remove(vertex.node());
        } else {
            for (E in : vertex.ins()) {
                Vertex<E> vin = vertices.get(in);
                if (vin.outs().size() <= 1) {
                    pruned = pruneIns(pruned, vin, beginEnd);
                } else {
                    Set<E> outs = vin.outs().remove(vertex.node());
                    pruned = pruned.put(Vertex.of(in, vin.ins(), outs));
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

    public Set<N> begin() {
        return begin;
    }

    public Set<N> end() {
        return end;
    }

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
            DirGraph<?> other = (DirGraph<?>) obj;
            return Objects.equals(vertices, other.vertices);
        }
    }

    public final static class Vertex<N> extends Struct3Impl<N, Set<N>, Set<N>> implements Mergeable<Vertex<N>> {
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

        @Override
        public Vertex<N> merge(Vertex<N>[] branches, int length) {
            Set<N> ins = ins();
            Set<N> outs = outs();
            for (int i = 0; i < length; i++) {
                ins = ins.addAll(branches[i].ins());
                outs = outs.addAll(branches[i].outs());
            }
            return new Vertex<N>(node(), ins, outs);
        }

        @Override
        public Vertex<N> getMerger() {
            return new Vertex<N>(node(), Set.of(), Set.of());
        }

        @Override
        public Class<?> getMeetClass() {
            return Vertex.class;
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
            ba[i] = branches[i].begin;
            ea[i] = branches[i].end;
            va[i] = branches[i].vertices;
        }
        Set<N> mb = begin.merge(null);
        Set<N> me = end.merge(null);
        QualifiedSet<N, Vertex<N>> mv = vertices.merge(va);
        return new DirGraph<N>(mb.filter(b -> mv.get(b).ins().isEmpty()).asSet(), me.filter(e -> mv.get(e).outs().isEmpty()).asSet(), mv);
    }

    @Override
    public DirGraph<N> getMerger() {
        return empty();
    }

}
