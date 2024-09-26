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

package org.modelingvalue.collections;

import org.modelingvalue.collections.impl.DirGraphImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.QuadFunction;

public interface DirGraph<N> extends ContainingCollection<Vertex<N>>, Mergeable<DirGraph<N>> {

    @SuppressWarnings("unchecked")
    @SafeVarargs
    static <E> DirGraph<E> of(E... edges) {
        if (edges.length == 0) {
            return DirGraphImpl.EMPTY;
        } else {
            return new DirGraphImpl<E>(edges);
        }
    }

    Set<Pair<N, N>> cycles();

    Set<Pair<N, N>> invCycles();

    Set<N>[] beginEnd();

    Set<N> begin();

    Set<N> end();

    Set<N> connected();

    Set<N> invConnected();

    Collection<N> nodes();

    Collection<Pair<N, N>> edges();

    QualifiedSet<N, Vertex<N>> vertices();

    Set<N> ins(N node);

    Set<N> outs(N node);

    Vertex<N> vertex(N node);

    boolean containsEdge(N from, N to);

    boolean containsNode(N node);

    List<N> topological();

    List<N> invTopological();

    <A> A dfs(A acc, QuadFunction<A, N, N, Boolean, A> func);

    <A> A invDfs(A acc, QuadFunction<A, N, N, Boolean, A> func);

    Dag<N> removeCycles();

    Dag<N> invRemoveCycles();

    @Override
    Dag<N> clear();

    DirGraph<N> removeNodes(Set<N> e);

    DirGraph<N> retainNodes(Set<N> e);

    @SuppressWarnings("unchecked")
    DirGraph<N> removeNodes(N... e);

    @SuppressWarnings("unchecked")
    DirGraph<N> retainNodes(N... e);

    DirGraph<N> removeDisconnected();

    DirGraph<N> invRemoveDisconnected();

    DirGraph<N> setBegin(Set<N> begin);

    DirGraph<N> setEnd(Set<N> end);

    @SuppressWarnings("unchecked")
    DirGraph<N> setBegin(N... begin);

    @SuppressWarnings("unchecked")
    DirGraph<N> setEnd(N... end);

    DirGraph<N> addEdge(N from, N to);

    DirGraph<N> removeEdge(N from, N to);

    DirGraph<N> clear(N node);

    DirGraph<N> clearOuts(N node);

    DirGraph<N> clearIns(N node);

    DirGraph<N> putBegin(N node, Set<N> outs);

    DirGraph<N> putEnd(N node, Set<N> ins);

    DirGraph<N> put(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> putOuts(N node, Set<N> outs);

    DirGraph<N> putIns(N node, Set<N> ins);

    DirGraph<N> add(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> addOuts(N node, Set<N> outs);

    DirGraph<N> addIns(N node, Set<N> ins);

    DirGraph<N> remove(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> removeOuts(N node, Set<N> outs);

    DirGraph<N> removeIns(N node, Set<N> ins);

    @SuppressWarnings("unchecked")
    DirGraph<N> addEdges(N... edges);

    @SuppressWarnings("unchecked")
    DirGraph<N> removeEdges(N... edges);

    @SuppressWarnings("unchecked")
    DirGraph<N> putBegin(N node, N... outs);

    @SuppressWarnings("unchecked")
    DirGraph<N> putEnd(N node, N... ins);

    @SuppressWarnings("unchecked")
    DirGraph<N> putOuts(N node, N... outs);

    @SuppressWarnings("unchecked")
    DirGraph<N> putIns(N node, N... ins);

    @SuppressWarnings("unchecked")
    DirGraph<N> addOuts(N node, N... outs);

    @SuppressWarnings("unchecked")
    DirGraph<N> addIns(N node, N... ins);

    @SuppressWarnings("unchecked")
    DirGraph<N> removeOuts(N node, N... outs);

    @SuppressWarnings("unchecked")
    DirGraph<N> removeIns(N node, N... ins);

    @Override
    DirGraph<N> remove(Object e);

    @Override
    DirGraph<N> removeAll(Collection<?> e);

    @Override
    DirGraph<N> add(Vertex<N> e);

    @Override
    DirGraph<N> addAll(Collection<? extends Vertex<N>> e);

    @Override
    DirGraph<N> addUnique(Vertex<N> e);

    @Override
    DirGraph<N> addAllUnique(Collection<? extends Vertex<N>> e);

    @Override
    DirGraph<N> replace(Object pre, Vertex<N> post);

    @Override
    DirGraph<N> replaceFirst(Object pre, Vertex<N> post);

}
