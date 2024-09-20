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
        if (vertex.ins().isEmpty()) {
            startEnd[0] = startEnd[0].remove(vertex.node());
        } else {
            for (E in : vertex.ins()) {
                Vertex<E> vin = vertices.get(in);
                if (vin.outs().size() <= 1) {
                    pruned = pruneIns(pruned, vin, startEnd);
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
