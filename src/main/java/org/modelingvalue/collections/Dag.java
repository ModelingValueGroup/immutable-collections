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
import org.modelingvalue.collections.util.TriFunction;

public interface Dag<N> extends DirGraph<N> {

    @SuppressWarnings("unchecked")
    static <E> Dag<E> of() {
        return DirGraphImpl.EMPTY;
    }

    <A> A dfs(A acc, TriFunction<A, N, N, A> func);

    <A> A invDfs(A acc, TriFunction<A, N, N, A> func);

    @Override
    Dag<N> removeNodes(Set<N> e);

    @Override
    Dag<N> retainNodes(Set<N> e);

    @SuppressWarnings("unchecked")
    @Override
    Dag<N> removeNodes(N... e);

    @SuppressWarnings("unchecked")
    @Override
    Dag<N> retainNodes(N... e);

    @Override
    Dag<N> removeDisconnected();

    @Override
    Dag<N> invRemoveDisconnected();

    @Override
    Dag<N> setBegin(Set<N> begin);

    @Override
    Dag<N> setEnd(Set<N> begin);

    @SuppressWarnings("unchecked")
    @Override
    Dag<N> setBegin(N... begin);

    @SuppressWarnings("unchecked")
    @Override
    Dag<N> setEnd(N... begin);

    @Override
    Dag<N> removeEdge(N from, N to);

    @Override
    Dag<N> clear(N node);

    @Override
    Dag<N> clearOuts(N node);

    @Override
    Dag<N> clearIns(N node);

    @Override
    Dag<N> putBegin(N node, Set<N> outs);

    @Override
    Dag<N> putEnd(N node, Set<N> ins);

    @Override
    Dag<N> remove(N node, Set<N> ins, Set<N> outs);

    @Override
    Dag<N> removeOuts(N node, Set<N> outs);

    @Override
    Dag<N> removeIns(N node, Set<N> ins);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> removeEdges(N... edges);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> putBegin(N node, N... outs);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> putEnd(N node, N... ins);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> removeOuts(N node, N... outs);

    @Override
    @SuppressWarnings("unchecked")
    Dag<N> removeIns(N node, N... ins);

    @Override
    Dag<N> remove(Object e);

    @Override
    Dag<N> removeAll(Collection<?> e);

}
