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

import org.modelingvalue.collections.impl.DagImpl;
import org.modelingvalue.collections.util.Mergeable;
import org.modelingvalue.collections.util.Pair;
import org.modelingvalue.collections.util.SerializableFunction;

public interface DirGraph<N> extends Collection<Vertex<N>>, Mergeable<DirGraph<N>> {

    @SuppressWarnings("rawtypes")
    SerializableFunction<Vertex, Object> NODE_OF_VERTEX = Vertex::node;
    @SuppressWarnings({"unchecked", "rawtypes"})
    Dag                                  EMPTY          = new DagImpl(Set.of(), Set.of(), QualifiedSet.of(NODE_OF_VERTEX));

    @SuppressWarnings("unchecked")
    static <E> Dag<E> empty() {
        return EMPTY;
    }

    Set<N> begin();

    Set<N> end();

    QualifiedSet<N, Vertex<N>> vertices();

    Set<N> ins(N node);

    Set<N> outs(N node);

    DirGraph<N> prune(N node);

    boolean containsNode(N node);

    boolean containsEdge(N from, N to);

    DirGraph<N> addNode(N node);

    DirGraph<N> removeNode(N node);

    DirGraph<N> addEdge(N from, N to);

    DirGraph<N> removeEdge(N from, N to);

    DirGraph<N> clear(N node);

    DirGraph<N> clearOuts(N node);

    DirGraph<N> clearIns(N node);

    DirGraph<N> put(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> putOuts(N node, Set<N> outs);

    DirGraph<N> putIns(N node, Set<N> ins);

    DirGraph<N> add(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> addOuts(N node, Set<N> outs);

    DirGraph<N> addIns(N node, Set<N> ins);

    DirGraph<N> remove(N node, Set<N> ins, Set<N> outs);

    DirGraph<N> removeOuts(N node, Set<N> outs);

    DirGraph<N> removeIns(N node, Set<N> ins);

    Set<Pair<N, N>> cycles();

    List<N> topological();

    Dag<N> removeCycles();

}
